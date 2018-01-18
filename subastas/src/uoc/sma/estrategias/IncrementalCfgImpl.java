package uoc.sma.estrategias;

import uoc.sma.behaviours.estrategias.CompradorInglesaIncrementalBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * Encapsula la estrategia de una puja inicial que cada cierto tiempo se va
 * incrementando en una cantidad fija sin que la puja llegue a superar el precio
 * de reserva marcado por el comprador
 *
 * @author Rodolfo de Benito
 *
 */
public class IncrementalCfgImpl implements ICfgApuesta {
	public final int INCREMENTOARITMETICO = 0;
	public final int INCREMENTOGEOMETRICO = 1;
	private int tipoIncremento;
	private double incremento; // Incremento de la puja
	private double puja;
	private double reserva; // La puja m�xima a la que llegar� el agente
	private CompradorInglesaIncrementalBehaviour comportamiento;

	public IncrementalCfgImpl(int tipoIncemento, double incremento,
			double puja, double reserva) {
		super();
		this.tipoIncremento = tipoIncemento;
		this.incremento = incremento;
		this.puja = puja;
		this.reserva = reserva;
	}

	public Behaviour getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(Behaviour comportamiento) {
		this.comportamiento = (CompradorInglesaIncrementalBehaviour) comportamiento;
	}

	public int getTipoIncemento() {
		return tipoIncremento;
	}

	public void setTipoIncemento(int tipoIncemento) {
		this.tipoIncremento = tipoIncemento;
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
}