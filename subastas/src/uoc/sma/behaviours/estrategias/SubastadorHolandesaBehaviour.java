package uoc.sma.behaviours.estrategias;

import java.util.Date;
import java.util.Vector;
import uoc.sma.agentes.AgenteSubastador;
import uoc.sma.datos.Subasta;
import uoc.sma.datos.Transaccion;
import uoc.sma.estrategias.SubastaHolandesaImpl;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

/**
 * Encapsula el comportamiento de un agente subastador de subasta Holandesa
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class SubastadorHolandesaBehaviour extends SubastadorBehaviour {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private AgenteSubastador agente;
	private Subasta s;
	private SubastaHolandesaImpl estrategiaVendedor;
	private AID idCasaSubastas;
	private double decremento = 15; // El decremento de precio que se aplicar�
	// en cada ronda
	private int contadorPujas = 0;
	private boolean finalSubasta = false;
	private boolean inferiorReserva = false;

	public SubastadorHolandesaBehaviour(Agent a, ACLMessage msg) {
		super(a, msg);
		logger.setLevel(Logger.INFO);
		agente = (AgenteSubastador) myAgent;
		s = agente.getSubasta();
		estrategiaVendedor = (SubastaHolandesaImpl) agente
				.getEstrategiaSubasta();
		// Localizar al Agente de Subastas en las p�ginas amarillas
		DFAgentDescription[] aCasaSubastas = getCasaSubastas();
		if (aCasaSubastas != null)
			this.idCasaSubastas = aCasaSubastas[0].getName();
	}

	@Override
	/**
	 * Prepara el mensaje CFP siguiente
	 */
	protected Vector prepareCfps(ACLMessage cfp) {
		cfp = agente.addAgentsToMessage(cfp);
		Vector mensajes = new Vector();
		mensajes.add(cfp);

		return mensajes;
	}

	@Override
	/**
	 * Comprueba si es necesaria otra ronda y la inicia si es preciso
	 */
	public int onEnd() {
		if (finalSubasta == false) {
			ACLMessage nextMsg = new ACLMessage(ACLMessage.CFP);
			nextMsg.setContent(String.valueOf(s.getPrecio()));
			logger.log(Logger.INFO, myAgent.getName() + ": Oferta de venta:"
					+ s.getPrecio());
			myAgent.addBehaviour(new SubastadorHolandesaBehaviour(myAgent,
					nextMsg));
		}
		return super.onEnd();
	}

	/**
	 * Gestiona las respuestas de todos los participantes en la subasta
	 */
	protected void handleAllResponses(Vector respuestas, Vector acceptances) {
		contadorPujas = 0;
		for (ACLMessage respuesta : (Vector<ACLMessage>) respuestas) {
			// Controlar los mensajes de tipo REFUSE (el agente no puja)
			if (respuesta.getPerformative() == ACLMessage.REFUSE) {
				logger.log(Logger.FINE, respuesta.getSender().getLocalName()
						+ " NO PUJA");
				// Controla los mensajes de tipo PROPOSE (las pujas)
			} else if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
				++contadorPujas;
				double puja = Double.parseDouble(respuesta.getContent());
				logger.log(Logger.FINE, "Agente:"
						+ respuesta.getSender().getLocalName() + " Puja="
						+ String.valueOf(puja));
				registrarTransaccion(
						new Transaccion(new Date(), s.getIdSubasta(), respuesta
								.getSender().getLocalName(), Transaccion.PUJA,
								puja), idCasaSubastas);
				// Puja ganadora
				s.setPrecio(puja);
				s.setMsgMejorOferta(respuesta);
				break;
			}
		}
		// Hay una puja
		if (contadorPujas > 0) {
			finalSubasta = true;
			logger.log(Logger.INFO, "FIN DE SUBASTA");
			// Enviar ACCEPT si hay un ganador
			if (s.getMsgMejorOferta() != null) {
				// Establece el ganador de la subasta
				s.setGanador(s.getMsgMejorOferta().getSender());
				logger.log(Logger.INFO, "Ganador de la subasta:" + s
						+ " el comprador "
						+ s.getMsgMejorOferta().getSender().getLocalName()
						+ " con una puja de " + s.getPrecio());
				registrarTransaccion(
						new Transaccion(new Date(), s.getIdSubasta(),
								s.getMsgMejorOferta().getSender()
										.getLocalName(), Transaccion.FIN,
								s.getPrecio()), idCasaSubastas);

				ACLMessage accept = s.getMsgMejorOferta().createReply();
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				accept.setContent(String.valueOf(s.getPrecio()));
				acceptances.add(accept);
				for (ACLMessage response : (Vector<ACLMessage>) respuestas) {
					if (s.getMsgMejorOferta() != response) {
						ACLMessage reject = response.createReply();
						reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
						reject.setContent(String.valueOf(s.getPrecio()));
						acceptances.add(reject);
					}
				}
			}
		} else {
			// Recalcular la nueva oferta de precio por parte del vendedor
			double nuevoPrecio = s.getPrecio() - decremento;
			if (nuevoPrecio >= s.getPrecioReserva()) {
				s.setPrecio(nuevoPrecio);
				registrarTransaccion(
						new Transaccion(new Date(), s.getIdSubasta(),
								myAgent.getLocalName(), Transaccion.OFERTA,
								s.getPrecio()), idCasaSubastas);
			} else
				inferiorReserva = true;
		}
		// En caso que no se reciba ninguna respuesta o el nuevo precio sea
		// inferior al de reserva
		// se da por finalizada la subasta sin ganador
		if (respuestas.size() == 0 || inferiorReserva) {
			logger.log(
					Logger.INFO,
					"No hay ganador de la subasta. Se ha agotado el tiempo m�ximo de espera denuevas pujas o el vendedor no bajar� mas el precio");
			for (ACLMessage response : (Vector<ACLMessage>) respuestas) {
				ACLMessage reject = response.createReply();
				reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
				reject.setContent(String.valueOf(s.getPrecioReserva()));
				acceptances.add(reject);
				finalSubasta = true;
			}
		}
	}
}
