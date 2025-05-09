package com.example.glicone;

import java.io.Serializable;

public class Medicamento implements Serializable {
    private String id;
    private String nome;
    private String dose;
    private String dataInicio;
    private String dataFim;
    private String hora;
    private String frequencia;
    private String userId;

    public Medicamento() {}

    public Medicamento(String nome, String dose, String hora, String dataInicio, String dataFim, String frequencia, String userId) {
        this.nome = nome;
        this.dose = dose;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.hora = hora;
        this.frequencia = frequencia;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDataFim() {
        return dataFim;
    }

    public void setDataFim(String dataFim) {
        this.dataFim = dataFim;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
