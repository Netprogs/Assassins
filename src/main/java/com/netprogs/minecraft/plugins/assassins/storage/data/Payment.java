package com.netprogs.minecraft.plugins.assassins.storage.data;

/*
 * Copyright (C) 2012 Scott Milne
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

public class Payment {

    public enum Type {
        cash, item
    }

    private double cashAmount;
    private int itemCount;
    private int itemId;

    private transient Type paymentTypeEnum;
    private String paymentType;

    public void setPaymentType(Type paymentType) {
        this.paymentTypeEnum = paymentType;
        this.paymentType = paymentType.toString();
    }

    public Type getPaymentType() {

        if (paymentTypeEnum == null) {
            paymentTypeEnum = Type.valueOf(paymentType);
        }
        return paymentTypeEnum;
    }

    public double getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
