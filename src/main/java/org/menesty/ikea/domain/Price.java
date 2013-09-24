package org.menesty.ikea.domain;

import java.math.BigDecimal;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:01 PM
 */
class Price {
    private BigDecimal netto;

    private BigDecimal brutto;

    private BigDecimal marga;

    private BigDecimal profit;

    private BigDecimal coating;


    BigDecimal getNetto() {
        return netto;
    }

    void setNetto(BigDecimal netto) {
        this.netto = netto;
    }

    BigDecimal getBrutto() {
        return brutto;
    }

    void setBrutto(BigDecimal brutto) {
        this.brutto = brutto;
    }

    BigDecimal getMarga() {
        return marga;
    }

    void setMarga(BigDecimal marga) {
        this.marga = marga;
    }

    BigDecimal getProfit() {
        return profit;
    }

    void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    BigDecimal getCoating() {
        return coating;
    }

    void setCoating(BigDecimal coating) {
        this.coating = coating;
    }
}


/*
        "IKEA_80034609","Detaliczna",y+2%=z,z+23%,2.0000,w/z *100,z-y=w
        "IKEA_80034609","Hurtowa",y+2% = z,z+23%,2.0000,w/z *100,z-y =w
        "IKEA_80034609","Specjalna",(x-23%)=y,x,0.0000,0.0000,0.0000
*/