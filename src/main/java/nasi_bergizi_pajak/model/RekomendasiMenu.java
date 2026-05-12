package nasi_bergizi_pajak.model;

public class RekomendasiMenu {
    private Recipe recipe;
    private KebutuhanGizi nilaiGizi;
    private double estimasiHarga;
    private double skor;
    private String status;
    private String statusBudget;
    private String statusStok;
    private double persentaseStok;

    public RekomendasiMenu() {
    }

    public RekomendasiMenu(Recipe recipe, KebutuhanGizi nilaiGizi, double estimasiHarga,
                           double skor, String status, String statusBudget,
                           String statusStok, double persentaseStok) {
        this.recipe = recipe;
        this.nilaiGizi = nilaiGizi;
        this.estimasiHarga = estimasiHarga;
        this.skor = skor;
        this.status = status;
        this.statusBudget = statusBudget;
        this.statusStok = statusStok;
        this.persentaseStok = persentaseStok;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getResep() {
        return recipe;
    }

    public void setResep(Recipe recipe) {
        this.recipe = recipe;
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

    public String getStatusBudget() {
        return statusBudget;
    }

    public void setStatusBudget(String statusBudget) {
        this.statusBudget = statusBudget;
    }

    public String getStatusStok() {
        return statusStok;
    }

    public void setStatusStok(String statusStok) {
        this.statusStok = statusStok;
    }

    public double getPersentaseStok() {
        return persentaseStok;
    }

    public void setPersentaseStok(double persentaseStok) {
        this.persentaseStok = persentaseStok;
    }
}