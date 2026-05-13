package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.dao.GiziDAO;
import nasi_bergizi_pajak.dao.RekomendasiDAO;
import nasi_bergizi_pajak.model.KebutuhanGizi;
import nasi_bergizi_pajak.model.RekomendasiMenu;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RekomendasiController {
    private final GiziDAO giziDAO;
    private final RekomendasiDAO rekomendasiDAO;

    public RekomendasiController() {
        this.giziDAO = new GiziDAO();
        this.rekomendasiDAO = new RekomendasiDAO();
    }

    public List<RekomendasiMenu> buatRekomendasiMenu(int userId) throws SQLException {
        int jumlahAnggota = giziDAO.hitungJumlahAnggotaKeluarga(userId);

        if (jumlahAnggota <= 0) {
            throw new IllegalStateException("Profil keluarga belum memiliki anggota. Lengkapi profil keluarga terlebih dahulu.");
        }

        KebutuhanGizi kebutuhanKeluarga = giziDAO.hitungKebutuhanGiziKeluarga(userId);

        if (kebutuhanKeluarga.getKalori() <= 0) {
            throw new IllegalStateException("Kebutuhan gizi keluarga belum dapat dihitung.");
        }

        Set<String> alergiKeluarga = giziDAO.ambilAlergiKeluarga(userId);
        Double budgetAktif = rekomendasiDAO.findActiveBudgetAmount(userId);

        List<RekomendasiDAO.RecommendationData> kandidat =
                rekomendasiDAO.listDataRekomendasi(userId, jumlahAnggota, alergiKeluarga);

        List<RekomendasiMenu> hasilRekomendasi = new ArrayList<>();

        for (RekomendasiDAO.RecommendationData data : kandidat) {
            String statusGizi = tentukanStatusGizi(data.getNutrition(), kebutuhanKeluarga);
            String statusBudget = tentukanStatusBudget(data.getEstimatedPrice(), budgetAktif);
            String statusStok = data.isStockSufficient() ? "STOK_CUKUP" : "STOK_KURANG";

            double skor = hitungSkorRekomendasi(
                    data.getNutrition(),
                    kebutuhanKeluarga,
                    data.getEstimatedPrice(),
                    budgetAktif,
                    data.isStockSufficient(),
                    data.getStockCoverage()
            );

            RekomendasiMenu rekomendasi = new RekomendasiMenu(
                    data.getRecipe(),
                    data.getNutrition(),
                    data.getEstimatedPrice(),
                    skor,
                    statusGizi,
                    statusBudget,
                    statusStok,
                    data.getStockCoverage()
            );

            hasilRekomendasi.add(rekomendasi);
        }

        hasilRekomendasi.sort(Comparator.comparingDouble(RekomendasiMenu::getSkor).reversed());

        return hasilRekomendasi;
    }

    public String evaluasiKecukupanGizi(KebutuhanGizi nilaiGiziMenu, KebutuhanGizi kebutuhanKeluarga) {
        return tentukanStatusGizi(nilaiGiziMenu, kebutuhanKeluarga);
    }

    private double hitungSkorRekomendasi(KebutuhanGizi nilaiGizi,
                                         KebutuhanGizi kebutuhan,
                                         double estimasiHarga,
                                         Double budgetAktif,
                                         boolean stokCukup,
                                         double persentaseStok) {

        double skorKalori = hitungPersentase(nilaiGizi.getKalori(), kebutuhan.getKalori());
        double skorProtein = hitungPersentase(nilaiGizi.getProtein(), kebutuhan.getProtein());
        double skorKarbo = hitungPersentase(nilaiGizi.getKarbohidrat(), kebutuhan.getKarbohidrat());
        double skorLemak = hitungPersentase(nilaiGizi.getLemak(), kebutuhan.getLemak());
        double skorSerat = hitungPersentase(nilaiGizi.getSerat(), kebutuhan.getSerat());

        double skorGizi = (skorKalori + skorProtein + skorKarbo + skorLemak + skorSerat) / 5.0;

        double penaltiHarga;
        if (budgetAktif != null && budgetAktif > 0) {
            penaltiHarga = Math.min(30.0, (estimasiHarga / budgetAktif) * 25.0);
        } else {
            penaltiHarga = Math.min(20.0, estimasiHarga / 100000.0);
        }

        double bonusStok = stokCukup
                ? 10.0
                : Math.max(0.0, Math.min(5.0, persentaseStok * 5.0));

        return Math.max(0.0, skorGizi - penaltiHarga + bonusStok);
    }

    private double hitungPersentase(double nilai, double target) {
        if (target <= 0) {
            return 0;
        }

        double persentase = nilai / target;
        return Math.min(persentase, 1.0) * 100.0;
    }

    private String tentukanStatusGizi(KebutuhanGizi nilaiGizi, KebutuhanGizi kebutuhan) {
        double skorKalori = hitungPersentase(nilaiGizi.getKalori(), kebutuhan.getKalori());
        double skorProtein = hitungPersentase(nilaiGizi.getProtein(), kebutuhan.getProtein());
        double skorKarbo = hitungPersentase(nilaiGizi.getKarbohidrat(), kebutuhan.getKarbohidrat());
        double skorLemak = hitungPersentase(nilaiGizi.getLemak(), kebutuhan.getLemak());
        double skorSerat = hitungPersentase(nilaiGizi.getSerat(), kebutuhan.getSerat());

        double rataRata = (skorKalori + skorProtein + skorKarbo + skorLemak + skorSerat) / 5.0;

        if (rataRata >= 80) {
            return "CUKUP";
        }

        return "KURANG";
    }

    private String tentukanStatusBudget(double estimasiHarga, Double budgetAktif) {
        if (budgetAktif == null) {
            return "BUDGET_TIDAK_TERSEDIA";
        }

        return estimasiHarga <= budgetAktif
                ? "SESUAI_BUDGET"
                : "MELEBIHI_BUDGET";
    }
}