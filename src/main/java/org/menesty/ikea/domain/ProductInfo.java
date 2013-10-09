package org.menesty.ikea.domain;

import java.util.List;
/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:55 PM
 */
public class ProductInfo {

    public enum Group {
        Regal, Decor, Lights, Kitchen, Bathroom, Textil, Full, Kids
    }

    private String originalArtNum;

    private String artNumber;

    private String name;

    private int boxCount;

    private double price;

    private int numberBox;

    private String shortName;

    private Group group;

    private List<ProductInfo> parts;

    private boolean part;

    private PackageInfo packageInfo;


    public List<ProductInfo> getParts() {
        return parts;
    }

    public void setParts(List<ProductInfo> parts) {
        this.parts = parts;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public boolean isPart(){
        return part;
    }

    public void setPart(boolean isPart){
        this.part = isPart;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public int getBoxCount() {
        return boxCount;
    }

    public void setBoxCount(int boxCount) {
        this.boxCount = boxCount;
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

    public void setNumberBox(int numberBox) {
        this.numberBox = numberBox;
    }

    public int getNumberBox() {
        return numberBox;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public String toString(){
        return artNumber+" | "+ originalArtNum +" | "+ name + " | " + shortName + " | " + boxCount;
    }
}
