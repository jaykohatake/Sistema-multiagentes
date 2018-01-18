package uoc.sma.datos;

import java.io.Serializable;
import java.util.Date;

/**
 * Encapsula los datos de una transacci�n u operaci�n realizada en una
 * subasta
 * 
 * @author Rodolfo de Benito
 */
public class Transaccion implements Serializable {
	private Date fecha;
	private int idSubasta;
	private String agente;
	private String operacion;
	private double valor;
	public static final String PUJA = "PUJA";
	public static final String OFERTA = "OFERTA";
	public static final String INICIO = "INICIO";
	public static final String FIN = "FIN";

	public Transaccion(Date fecha, int idSubasta, String agente,
			String operacion, double valor) {
		super();
		this.fecha = fecha;
		this.idSubasta = idSubasta;
		this.agente = agente;
		this.operacion = operacion;
		this.valor = valor;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public int getIdSubasta() {
		return idSubasta;
	}

	public void setIdSubasta(int idSubasta) {
		this.idSubasta = idSubasta;
	}

	public String getAgente() {
		return agente;
	}

	public void setAgente(String agente) {
		this.agente = agente;
	}

	public String getOperacion() {
		return operacion;
	}

	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public String toString() {
		String cadena = "Transaccion Fecha:" + getFecha() + " Id. subasta:"
				+ getIdSubasta() + " Agente:" + getAgente() + " Operacion:"
				+ getOperacion() + " Valor:" + getValor();
		return cadena;
	}
}