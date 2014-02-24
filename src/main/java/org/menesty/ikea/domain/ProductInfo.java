package org.menesty.ikea.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:55 PM
 */

@Entity
public class ProductInfo extends Identifiable {

    private int wat;

    public int getWat() {
        return wat;
    }

    public void setWat(int wat) {
        this.wat = wat;
    }

    public enum Group {

        Regal("Regal"), Decor("Dekoracja"), Lights("Oświetlenie"), Kitchen("Kuchnia"), Bathroom("Łazienka"), Textile("Tekstylia"), Full(), Storing("Przechowywanie"), Family("Family"), Kids("dla Dzieci"), Combo("", false), Unknown("Unknown", false);

        private final boolean defaults;

        private final String title;

        Group() {
            this("", true);
        }

        Group(String title) {
            this(title, true);
        }

        Group(String title, boolean defaults) {
            this.defaults = defaults;
            this.title = title;
        }

        public static List<Group> general() {
            List<Group> groups = new ArrayList<>();

            for (Group value : values())
                if (value.defaults) groups.add(value);

            return groups;
        }

        public String getTitel() {
            return title;
        }
    }

    private String originalArtNum;

    private String name;

    private double price;

    private String shortName;

    private String uaName;

    @Enumerated(EnumType.STRING)
    private Group group = Group.Unknown;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ProductPart> parts;

    @Embedded()
    private PackageInfo packageInfo = new PackageInfo();

    private boolean verified;

    public String getUaName() {
        return uaName;
    }

    public void setUaName(String uaName) {
        this.uaName = uaName;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public List<ProductPart> getParts() {
        return parts;
    }

    public void setParts(List<ProductPart> parts) {
        for (ProductPart item : parts)
            item.parent = this;
        this.parts = parts;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getOriginalArtNum() {
        return originalArtNum;
    }

    public void setOriginalArtNum(String originalArtNum) {
        this.originalArtNum = originalArtNum;
    }

    public String getArtNumber() {
        return formatProductId(originalArtNum);
    }

    public static String cleanProductId(final String artNumber) {
        if (artNumber == null || artNumber.length() < 1)
            throw new RuntimeException("ArtNumber can't be null or less then one length");

        String productId = artNumber;
        boolean prefix = false;

        if (Character.isAlphabetic(productId.charAt(0))) {
            productId = productId.substring(1);
            prefix = true;
        }
        return (prefix ? artNumber.charAt(0) : "") + productId.replaceAll("\\D+", "");
    }

    public static String formatProductId(final String artNumber) {
        String productId = artNumber;
        if (Character.isAlphabetic(artNumber.charAt(0)))

            productId = productId.substring(1);
        productId = productId.replaceAll("\\D+", "");

        if (productId.length() < 8)
            productId = String.format("%08d%n", Integer.valueOf(productId));

        int lastPos = productId.length();
        return (Character.isAlphabetic(artNumber.charAt(0)) ? artNumber.charAt(0) : "")
                + (productId.substring(0, lastPos - 5) + "-" + productId.substring(lastPos - 5, lastPos - 2) + "-" + productId.substring(lastPos - 2)).intern();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductInfo that = (ProductInfo) o;

        if (originalArtNum != null ? !originalArtNum.equals(that.originalArtNum) : that.originalArtNum != null)
            return false;

        return true;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public String toString() {
        return getArtNumber() + " | " + originalArtNum + " | " + name + " | " + shortName + " | " + (packageInfo != null ? packageInfo.getBoxCount() : "0") + " | " + parts;
    }

}
