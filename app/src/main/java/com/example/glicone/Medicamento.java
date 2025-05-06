package com.example.glicone;

public class Medicamento {
    private String nome;
    private String descricao;
    private String dataHora;
    private String userId;

    public Medicamento() {}

    public Medicamento(String nome, String descricao, String dataHora, String userId) {
        this.nome = nome;
        this.descricao = descricao;
        this.dataHora = dataHora;
        this.userId = userId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
