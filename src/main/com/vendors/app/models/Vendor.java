package com.vendors.app.models;

import org.json.JSONObject;

import com.vendors.app.utils.Formatter;
import com.vendors.app.lib.enums.Rank;

import java.util.HashMap;
import java.util.Map;

public class Vendor {
    private final long cedula;
    private long parentId;
    private String name;
    private Rank previousRank;
    private Rank currentRank;
    private double salesMonthly;
    private final Map<String, Double> commissionType;
    private double commission;

    public Vendor (long cedula, String name, Rank currentRank, double salesMonthly) {
        this(cedula, name, currentRank, salesMonthly, 0);
    }

    public Vendor(long cedula, String name, Rank currentRank, double salesMonthly, long parentId) {
        this.cedula = cedula;
        this.name = name;
        this.previousRank = currentRank;
        this.currentRank = currentRank;
        this.salesMonthly = salesMonthly;
        this.commissionType = new HashMap<>();
        this.parentId = parentId;
    }

    public long getCedula() {
        return cedula;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rank getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(Rank currentRank) {
        this.previousRank = this.currentRank;
        this.currentRank = currentRank;
    }

    public Rank getPreviousRank() {
        return previousRank;
    }

    public void setPreviousRank(Rank previousRank) {
        this.previousRank = previousRank;
    }

    public double getSalesMonthly() {
        return salesMonthly;
    }

    public void setSalesMonthly(double salesMonthly) {
        this.salesMonthly = salesMonthly;
    }

    public double getCommission() {
        return commission + calculateVendorCommission();
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getCommissionType(String type) {
        return commissionType.get(type);
    }

    public void setCommissionType(String type, double percentage) {
        commissionType.put(type, percentage);
    }

    public String getCommissionDescription() {
        String description = "";
        String separator = "";

        for (Map.Entry<String, Double> entry : commissionType.entrySet()) {
            if (entry.getValue() > 0) {
                description += separator + Formatter.formatDecimals(entry.getValue() * 100) + "% " + entry.getKey();
                separator = " + ";
            }
        }

        return description;
    }

    public JSONObject toJSON() {
        JSONObject vendor = new JSONObject();
        vendor.put("name", name);
        vendor.put("prevrank", previousRank.getType());
        vendor.put("currank", currentRank.getType());
        vendor.put("comision", getCommission());
        vendor.put("comisiondesc", getCommissionDescription());

        return vendor;
    }

    public double calculateVendorCommission() {
        double calculatedCommission = 0;

        for (Map.Entry<String, Double> entry : commissionType.entrySet()) {
            if (!entry.getKey().matches("level\\s\\d*$") && entry.getValue() > 0) {
                calculatedCommission += entry.getValue() * salesMonthly;
            }
        }

        return calculatedCommission;
    }

    public void addLevelCommission(double value) {
        commission += value;
    }

    public void assignPersonalCommission() {
        String type = "personal";
        double percentage = 0;

        switch (currentRank) {
            case COBRE:
                percentage = 10;
                break;
            case BRONCE:
                percentage = 15;
                break;
            case PLATA:
                percentage = 20;
                break;
            case ORO:
                percentage = 25;
                break;
        }

        percentage = percentage / 100;

        setCommissionType(type, percentage);
    }

    public void assignLevelUpCommission() {
        if (previousRank == currentRank || currentRank.getValue() < previousRank.getValue()) {
            return;
        }

        String type = "level up";
        double percentage = 0;

        if (currentRank == Rank.BRONCE) {
            percentage = 5;
        }

        if (currentRank == Rank.PLATA) {
            percentage = 10;
        }

        if (currentRank == Rank.ORO) {
            percentage = 15;
        }

        percentage = percentage / 100;

        setCommissionType(type, percentage);
    }

    public void assignLevelCommission(int maxLevel) {
        for (int number = 1; number <= maxLevel; number++) {
            if (number > 3) break;

            double percentage = 0;

            switch (number) {
                case 1:
                    percentage = 1;
                    break;
                case 2:
                    percentage = 2;
                    break;
                case 3:
                    percentage = 3;
                    break;
            }

            percentage = percentage / 100;

            setCommissionType("level " + number, percentage);
        }
    }

}
