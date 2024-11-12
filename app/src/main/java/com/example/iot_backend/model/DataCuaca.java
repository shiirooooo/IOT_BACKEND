package com.example.iot_backend.model;

import com.google.gson.annotations.SerializedName;

public class DataCuaca {
    @SerializedName("rata_suhu")
    private float rataSuhu;

    @SerializedName("suhu_tertinggi")
    private float suhuTertinggi;

    // Getter
    public float getRataSuhu() { return rataSuhu; }
    public float getSuhuTertinggi() { return suhuTertinggi; }
}
