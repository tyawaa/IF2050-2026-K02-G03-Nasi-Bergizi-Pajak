package nasi_bergizi_pajak.model;

public class Resep {
    private int recipeId;
    private String nama;
    private String deskripsi;
    private int servingSize;
    private String status;

    public Resep() {
    }

    public Resep(int recipeId, String nama, String deskripsi, int servingSize, String status) {
        this.recipeId = recipeId;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.servingSize = servingSize;
        this.status = status;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}