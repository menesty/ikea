package org.menesty.ikea.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:55 PM
 */
public class ProductInfo {

    private int wat;

    public int getWat() {
        return wat;
    }

    public void setWat(int wat) {
        this.wat = wat;
    }

    public enum Group {

        Regal, Decor, Lights, Kitchen, Bathroom, Textile, Full, Storing, Family, Kids, Combo(false);

        private final boolean defaults;

        Group() {
            defaults = true;
        }

        Group(boolean defaults) {
            this.defaults = defaults;
        }

        public static List<Group> general() {
            List<Group> groups = new ArrayList<>();

            for (Group value : values())
                if (value.defaults) groups.add(value);

            return groups;
        }
    }

    private String originalArtNum;

    private String artNumber;

    private String name;

    private double price;

    private String shortName;

    private Group group;

    private List<ProductPart> parts;

    private PackageInfo packageInfo = new PackageInfo();

    private boolean verified;

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
        return artNumber;
    }

    public void setArtNumber(String artNumber) {
        this.artNumber = artNumber;
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
        return artNumber + " | " + originalArtNum + " | " + name + " | " + shortName + " | " + (packageInfo != null ? packageInfo.getBoxCount() : "0") + " | " + parts;
    }
}
