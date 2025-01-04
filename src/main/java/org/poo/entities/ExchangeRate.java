package org.poo.entities;

public final class ExchangeRate {
    private String from;
    private String to;
    private double rate;

    public ExchangeRate(final String from, final String to, final double rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
    }

    /**
     * Seteaza valuta de la care se face conversia
     * @param from
     */
    public void setFrom(final String from) {
        this.from = from;
    }
    /**
     * Returneaza valuta de la care se face conversia
     * @return valuta de la care se face conversia
     */
    public String getFrom() {
        return from;
    }
    /**
     * Returneaza valuta in care se face conversia
     * @return valuta in care se face conversia
     */
    public String getTo() {
        return to;
    }
    /**
     * Seteaza valuta in care se face conversia
     * @param to valuta in care se face conversia
     */
    public void setTo(final String to) {
        this.to = to;
    }
    /**
     * Returneaza rata de conversie
     * @return rata de conversie
     */
    public double getRate() {
        return rate;
    }
    /**
     * Seteaza rata de conversie
     * @param rate rata de conversie
     */
    public void setRate(final double rate) {
        this.rate = rate;
    }
}
