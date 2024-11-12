package com.example.iot_backend.api;

import com.example.iot_backend.model.DataCuaca;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DataCuacaService {
    @GET("datacuaca.php") // Sesuaikan dengan path endpoint PHP Anda
    Call<DataCuaca> getDataCuaca();
}
