package uoc.sma.estrategias;

import uoc.sma.behaviours.estrategias.CompradorHolandesaIncrementoDinamicoBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * Encapsula la estrategia de una �nica puja
 *
 * En principio el agente considera pujar en la subasta por el precio
 * configurado en su atributo Puja Si se supera el n�mero de rondas "Rondas"
 * entonces incrementar� el valor de aceptaci�n de la puja siempre y cuando
 * no llegue al precio de reserva
 *
 * @author Rodolfo de Benito
 *
 */
public class ApuestaUnicaIncrementoDinamicoCfgImpl implements ICfgApuesta {
	private double incremento; // Incremento del valor de aceptaci�n de la
								// puja
	private double puja; // Valor de aceptaci�n por el que est� dispuesto a
							// pujar si no se alcanza el n�mero de rondas
							// estipulado
	private double reserva; // La puja m�xima a la que llegar� el agente
	private int rondas; // N�mero de rondas que esperar� para subir el valor
						// de aceptaci�n de la puja
	private int contadorRondas; // Contabiliza el n�mero de rondas que lleva
								// un comprador en una subasta
	private CompradorHolandesaIncrementoDinamicoBehaviour comportamiento;

	public ApuestaUnicaIncrementoDinamicoCfgImpl(double incremento,
			double puja, double reserva, int rondas) {
		super();
		this.incremento = incremento;
		this.puja = puja;
		this.reserva = reserva;
		this.rondas = rondas;
		this.contadorRondas = 0;
	}

	public Behaviour getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(Behaviour comportamiento) {
		this.comportamiento = (CompradorHolandesaIncrementoDinamicoBehaviour) comportamiento;
	}

	public int getRondas() {
		return rondas;
	}

	public void setRondas(int rondas) {
		this.rondas = rondas;
	}

	public double getIncremento() {
		return incremento;
	}

	public void setIncremento(double incremento) {
		this.incremento = incremento;
	}

	public double getPuja() {
		return puja;
	}

	public void setPuja(double puja) {
		this.puja = puja;
	}

	public double getReserva() {
		return reserva;
	}

	public void setReserva(double reserva) {
		this.reserva = reserva;
	}

	public int getContadorRondas() {
		return contadorRondas;
	}

	public void setContadorRondas(int contadorRondas) {
		this.contadorRondas = contadorRondas;
	}
}
