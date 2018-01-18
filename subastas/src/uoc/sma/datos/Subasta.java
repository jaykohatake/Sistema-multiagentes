package uoc.sma.datos;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Clase que encapsula los datos propios de la subasta
 *
 * @author Rodolfo de Benito
 */
public class Subasta implements Serializable {
	public static final int SUBASTA_INGLESA = 1;
	public static final int SUBASTA_HOLANDESA = 2;
	private static final long serialVersionUID = 1L;
	private int idSubasta;
	private Date fechaInicio;
	private AID agenteSubastador;
	private String objetoSubastado;
	private double precio;
	private double precioReserva;
	private boolean activa;
	private int tipoSubasta;
	private AID ganador;
	private ACLMessage msgMejorOferta;
	private ArrayList<AID> suscriptores;

	public Subasta(int idSubasta, Date fechaInicio, String objetoSubastado,
			double precio, double precioReserva, boolean activa, int tipoSubasta) {
		super();
		this.idSubasta = idSubasta;
		this.fechaInicio = fechaInicio;
		this.objetoSubastado = objetoSubastado;
		this.precio = precio;
		this.precioReserva = precioReserva;
		this.activa = activa;
		this.tipoSubasta = tipoSubasta;
		suscriptores = new ArrayList<AID>();
	}

	public ACLMessage getMsgMejorOferta() {
		return msgMejorOferta;
	}

	public void setMsgMejorOferta(ACLMessage msgMejorOferta) {
		this.msgMejorOferta = msgMejorOferta;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public int getIdSubasta() {
		return idSubasta;
	}

	public void setIdSubasta(int idSubasta) {
		this.idSubasta = idSubasta;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getObjetoSubastado() {
		return objetoSubastado;
	}

	public void setObjetoSubastado(String objetoSubastado) {
		this.objetoSubastado = objetoSubastado;
	}

	public double getPrecioReserva() {
		return precioReserva;
	}

	public void setPrecioReserva(double precioReserva) {
		this.precioReserva = precioReserva;
	}

	public boolean isActiva() {
		return activa;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}

	public int getTipoSubasta() {
		return tipoSubasta;
	}

	public void setTipoSubasta(int tipoSubasta) {
		this.tipoSubasta = tipoSubasta;
	}

	public AID getAgenteSubastador() {
		return agenteSubastador;
	}

	public void setAgenteSubastador(AID agenteSubastador) {
		this.agenteSubastador = agenteSubastador;
	}

	public AID getGanador() {
		return ganador;
	}

	public void setGanador(AID ganador) {
		this.ganador = ganador;
	}

	public ArrayList<AID> getSuscriptores() {
		return suscriptores;
	}

	public void setSuscriptores(ArrayList<AID> suscriptores) {
		this.suscriptores = suscriptores;
	}

	@Override
	public String toString() {
		String cadena;
		cadena = "Subasta Id:" + getIdSubasta() + " " + getObjetoSubastado()
				+ " Precio:" + getPrecio() + " Inicio:" + getFechaInicio();
		return cadena;
	}
}