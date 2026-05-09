package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.dao.GiziDAO;
import nasi_bergizi_pajak.dao.ResepDAO;
import nasi_bergizi_pajak.model.KebutuhanGizi;
import nasi_bergizi_pajak.model.RekomendasiMenu;
import nasi_bergizi_pajak.model.Resep;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RekomendasiController {
    private final GiziDAO giziDAO;
    private final ResepDAO resepDAO;

    public RekomendasiController() {
        this.giziDAO = new GiziDAO();
        this.resepDAO = new ResepDAO();
    }

    public List<RekomendasiMenu> buatRekomendasiMenu(int userId) throws SQLException {
        KebutuhanGizi kebutuhanKeluarga = giziDAO.hitungKebutuhanGiziKeluarga(userId);
        List<Resep> daftarResep = resepDAO.getResepAktif();
        List<RekomendasiMenu> hasilRekomendasi = new ArrayList<>();

        for (Resep resep : daftarResep) {
            KebutuhanGizi nilaiGizi = resepDAO.hitungNilaiGiziResep(resep.getRecipeId());
            double estimasiHarga = resepDAO.hitungEstimasiHargaResep(resep.getRecipeId());
            double skor = hitungSkorRekomendasi(nilaiGizi, kebutuhanKeluarga, estimasiHarga);
            String status = tentukanStatusGizi(nilaiGizi, kebutuhanKeluarga);

            RekomendasiMenu rekomendasi = new RekomendasiMenu(
                    resep,
                    nilaiGizi,
                    estimasiHarga,
                    skor,
                    status
            );

            hasilRekomendasi.add(rekomendasi);
        }

        hasilRekomendasi.sort(Comparator.comparingDouble(RekomendasiMenu::getSkor).reversed());

        return hasilRekomendasi;
    }

    public String evaluasiKecukupanGizi(KebutuhanGizi nilaiGiziMenu, KebutuhanGizi kebutuhanKeluarga) {
        return tentukanStatusGizi(nilaiGiziMenu, kebutuhanKeluarga);
    }

    private double hitungSkorRekomendasi(KebutuhanGizi nilaiGizi, KebutuhanGizi kebutuhan, double estimasiHarga) {
        double skorKalori = hitungPersentase(nilaiGizi.getKalori(), kebutuhan.getKalori());
        double skorProtein = hitungPersentase(nilaiGizi.getProtein(), kebutuhan.getProtein());
        double skorKarbo = hitungPersentase(nilaiGizi.getKarbohidrat(), kebutuhan.getKarbohidrat());
        double skorLemak = hitungPersentase(nilaiGizi.getLemak(), kebutuhan.getLemak());
        double skorSerat = hitungPersentase(nilaiGizi.getSerat(), kebutuhan.getSerat());

        double skorGizi = (skorKalori + skorProtein + skorKarbo + skorLemak + skorSerat) / 5.0;
        double penaltiHarga = estimasiHarga / 100000.0;

        return Math.max(0, skorGizi - penaltiHarga);
    }

    private double hitungPersentase(double nilai, double target) {
        if (target <= 0) {
            return 0;
        }

        double persentase = nilai / target;
        return Math.min(persentase, 1.0) * 100.0;
    }

    private String tentukanStatusGizi(KebutuhanGizi nilaiGizi, KebutuhanGizi kebutuhan) {
        double persentaseKalori = hitungPersentase(nilaiGizi.getKalori(), kebutuhan.getKalori());

        if (persentaseKalori >= 80) {
            return "CUKUP";
        }

        return "KURANG";
    }
}