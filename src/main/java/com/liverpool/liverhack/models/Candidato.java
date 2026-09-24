package com.liverpool.liverhack.models;

import jakarta.persistence.*;

@Entity
@Table(name = "candidatos")
public class Candidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String correo;
    private String telefono;
    private String puestoActual;
    private String empresaActual;
    private Double compensacionActual;
    private Double compensacionDeseada;
    private String escolaridad;
    private String fortalezas;
    private String idiomas;

    private String statusProceso;
    private String statusJustificacion;
    private String fechaEntrevista;

    private Integer scorePsicometrico;
    private String recomendacionAT;
    private String notasEntrevista;

    private String fechaIngreso;

    public Candidato() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPuestoActual() { return puestoActual; }
    public void setPuestoActual(String puestoActual) { this.puestoActual = puestoActual; }
    public String getEmpresaActual() { return empresaActual; }
    public void setEmpresaActual(String empresaActual) { this.empresaActual = empresaActual; }
    public Double getCompensacionActual() { return compensacionActual; }
    public void setCompensacionActual(Double compensacionActual) { this.compensacionActual = compensacionActual; }
    public Double getCompensacionDeseada() { return compensacionDeseada; }
    public void setCompensacionDeseada(Double compensacionDeseada) { this.compensacionDeseada = compensacionDeseada; }
    public String getEscolaridad() { return escolaridad; }
    public void setEscolaridad(String escolaridad) { this.escolaridad = escolaridad; }
    public String getFortalezas() { return fortalezas; }
    public void setFortalezas(String fortalezas) { this.fortalezas = fortalezas; }
    public String getIdiomas() { return idiomas; }
    public void setIdiomas(String idiomas) { this.idiomas = idiomas; }
    public String getStatusProceso() { return statusProceso; }
    public void setStatusProceso(String statusProceso) { this.statusProceso = statusProceso; }
    public String getStatusJustificacion() { return statusJustificacion; }
    public void setStatusJustificacion(String statusJustificacion) { this.statusJustificacion = statusJustificacion; }
    public String getFechaEntrevista() { return fechaEntrevista; }
    public void setFechaEntrevista(String fechaEntrevista) { this.fechaEntrevista = fechaEntrevista; }
    public Integer getScorePsicometrico() { return scorePsicometrico; }
    public void setScorePsicometrico(Integer scorePsicometrico) { this.scorePsicometrico = scorePsicometrico; }
    public String getRecomendacionAT() { return recomendacionAT; }
    public void setRecomendacionAT(String recomendacionAT) { this.recomendacionAT = recomendacionAT; }
    public String getNotasEntrevista() { return notasEntrevista; }
    public void setNotasEntrevista(String notasEntrevista) { this.notasEntrevista = notasEntrevista; }
    public String getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(String fechaIngreso) { this.fechaIngreso = fechaIngreso; }
}