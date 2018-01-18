package uoc.sma.behaviours.estrategias;

import java.util.Date;
import java.util.Vector;
import uoc.sma.agentes.AgenteSubastador;
import uoc.sma.datos.Subasta;
import uoc.sma.datos.Transaccion;
import uoc.sma.estrategias.SubastaInglesaImpl;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

/**
 * Encapsula el comportamiento de un agente subastador de subasta inglesa
 *
 * @author Rodolfo de Benito
 *
 */
public class SubastadorInglesaBehaviour extends SubastadorBehaviour {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private AgenteSubastador agente;
	private Subasta s;
	private SubastaInglesaImpl estrategiaVendedor;
	private AID idCasaSubastas;
	private int contadorPujas = 0;
	private boolean finalSubasta = false;

	public SubastadorInglesaBehaviour(Agent a, ACLMessage msg) {
		super(a, msg);
		agente = (AgenteSubastador) myAgent;
		s = agente.getSubasta();
		estrategiaVendedor = (SubastaInglesaImpl) agente.getEstrategiaSubasta();
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
			myAgent.addBehaviour(new SubastadorInglesaBehaviour(myAgent,
					nextMsg));
		}
		return super.onEnd();
	}

	/**
	 * Gestiona las respuestas de todos los participantes en la subasta
	 */
	protected void handleAllResponses(Vector respuestas, Vector acceptances) {
		ACLMessage mejorOfertaMsg = null;
		double mejorPuja = 0;
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
				// Comprueba si es la mejor puja
				if (puja > mejorPuja) {
					mejorPuja = puja;
					s.setPrecio(mejorPuja);
					mejorOfertaMsg = respuesta;
					s.setMsgMejorOferta(mejorOfertaMsg);
				}
			}
		}
		logger.log(Logger.INFO,
				"Puja m�s alta en esta ronda: " + String.valueOf(mejorPuja));
		if (contadorPujas == 0) {
			finalSubasta = true;
			logger.log(Logger.FINE, "No hay m�s pujas");
			// Enviar ACCEPT si hay un ganador
			if ((s.getMsgMejorOferta()) != null
					&& (s.getPrecio() > s.getPrecioReserva())) {
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
			} else {
				logger.log(Logger.INFO,
						"No hay ganador de la subasta. Las pujas no superan el precio de reserva: "
								+ s.getPrecioReserva());
				for (ACLMessage response : (Vector<ACLMessage>) respuestas) {
					ACLMessage reject = response.createReply();
					reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
					reject.setContent(String.valueOf(s.getPrecioReserva()));
					acceptances.add(reject);
				}
			}
		}
	}
}