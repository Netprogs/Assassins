package com.netprogs.minecraft.plugins.assassins.io;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;

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

/**
 * This adapter converts the old price "payment" into the new payment object providing backward-compatibility with
 * loading of older configuration files.
 */
public class JsonPaymentAdapter implements JsonDeserializer<Payment> {

    public Payment deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
            throws JsonParseException {

        // Now, when reading, we may still get a "payment: 5000".
        // So we want to check to see if the value is an integer, if it is, convert into a Payment object instead.

        if (elem.isJsonPrimitive()) {

            // create a Payment object
            Payment payment = new Payment();
            payment.setCashAmount(elem.getAsDouble());
            payment.setPaymentType(Payment.Type.cash);
            return payment;

        } else {

            final JsonObject wrapper = (JsonObject) elem;

            JsonElement cashAmount = wrapper.get("cashAmount");
            JsonElement itemCount = wrapper.get("itemCount");
            JsonElement itemId = wrapper.get("itemId");
            JsonElement paymentType = wrapper.get("paymentType");

            // create a Payment object
            Payment payment = new Payment();
            payment.setCashAmount(cashAmount.getAsDouble());
            payment.setItemCount(itemCount.getAsInt());
            payment.setItemId(itemId.getAsInt());
            payment.setPaymentType(Payment.Type.valueOf(paymentType.getAsString()));
            return payment;
        }
    }
}
