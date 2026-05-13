package nasi_bergizi_pajak.model;

public class KebutuhanGizi {
    private double kalori;
    private double protein;
    private double karbohidrat;
    private double lemak;
    private double serat;

    public KebutuhanGizi() {
    }

    public KebutuhanGizi(double kalori, double protein, double karbohidrat, double lemak, double serat) {
        this.kalori = kalori;
        this.protein = protein;
        this.karbohidrat = karbohidrat;
        this.lemak = lemak;
        this.serat = serat;
    }

    public double getKalori() {
        return kalori;
    }

    public void setKalori(double kalori) {
        this.kalori = kalori;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getKarbohidrat() {
        return karbohidrat;
    }

    public void setKarbohidrat(double karbohidrat) {
        this.karbohidrat = karbohidrat;
    }

    public double getLemak() {
        return lemak;
    }

    public void setLemak(double lemak) {
        this.lemak = lemak;
    }

    public double getSerat() {
        return serat;
    }

    public void setSerat(double serat) {
        this.serat = serat;
    }

    public void tambah(KebutuhanGizi gizi) {
        this.kalori += gizi.getKalori();
        this.protein += gizi.getProtein();
        this.karbohidrat += gizi.getKarbohidrat();
        this.lemak += gizi.getLemak();
        this.serat += gizi.getSerat();
    }
}