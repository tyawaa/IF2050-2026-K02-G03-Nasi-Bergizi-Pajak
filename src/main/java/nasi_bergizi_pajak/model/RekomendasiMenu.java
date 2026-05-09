package nasi_bergizi_pajak.model;

public class RekomendasiMenu {
    private Resep resep;
    private KebutuhanGizi nilaiGizi;
    private double estimasiHarga;
    private double skor;
    private String status;

    public RekomendasiMenu() {
    }

    public RekomendasiMenu(Resep resep, KebutuhanGizi nilaiGizi, double estimasiHarga, double skor, String status) {
        this.resep = resep;
        this.nilaiGizi = nilaiGizi;
        this.estimasiHarga = estimasiHarga;
        this.skor = skor;
        this.status = status;
    }

    public Resep getResep() {
        return resep;
    }

    public void setResep(Resep resep) {
        this.resep = resep;
    }

    public KebutuhanGizi getNilaiGizi() {
        return nilaiGizi;
    }

    public void setNilaiGizi(KebutuhanGizi nilaiGizi) {
        this.nilaiGizi = nilaiGizi;
    }

    public double getEstimasiHarga() {
        return estimasiHarga;
    }

    public void setEstimasiHarga(double estimasiHarga) {
        this.estimasiHarga = estimasiHarga;
    }

    public double getSkor() {
        return skor;
    }

    public void setSkor(double skor) {
        this.skor = skor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}