package inge.progetto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Rappresenta un attuatore ovvero un dispositivo ,tramite il quale, il sistema domotivo compiere determinate azioni
 * (accensione/spegnimento luce, regolazione temperatura, apertura/chiusura finestre) che influenzano il comportamento
 * delle sottounita da queste gestite. Ogni attuatore &egrave; definito da un {@link #nome}, una {@link #categoria} che ne
 * descrive inoltre tutte le possibili modalit&agrave; operative che possono essere esibite da attuatori simili, e da uno
 * stato di attivazione che indica se &egrave; spento o acceso.
 *
 * @author Parampal Singh, Mattia Nodari
 */
public class Attuatore implements Serializable {

    /**
     * nome dell'attuatore
     */
    private String nome;

    /**
     * Carattestica di associazione ad artefatto
     */
    private boolean singolo;
    /**
     * lista di artefatto comandato dall'attuatore
     */
    private ArrayList<Artefatto> listaComandati;

    /**
     *  categoria a cui appartiene l'attuatore
     *  @see CategoriaAttuatore
     */
    private CategoriaAttuatore categoria;


    /**
     * Modalit&agrave; operativa attualmente esibita dall'attuatore. Le possibili modalit&agrave; operative
     * sono definite nella sua {@link #categoria}.
     */
    private String modalitaAttuale;

    /**
     * Lo stato(spento o acceso) dell'attuatore
     */
    private boolean statoAttivazione;

    public Attuatore(String nome, CategoriaAttuatore categoria, String modalitaAttuale, boolean singolo) {
        this.nome = nome + "_" + categoria.getNome();
        this.categoria = categoria;
        this.modalitaAttuale = modalitaAttuale;
        this.statoAttivazione = true;
        this.singolo = singolo;
        listaComandati = new ArrayList<>();
    }

    /**Fornisce il nome dell'attuatpre
     * @return  nome dell'attuatore
     */
    public String getNome() {
        return nome;
    }

    /**Permette di modificare il nome dell'attuatore
     * @param nome nuovo nome da assegnare all'attuatore
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**Permette di ottenere la categoria a cui appartiene l'attuatore
     * @return la categoria a cui appartiene l'attuatore
     */
    public CategoriaAttuatore getCategoria() {
        return categoria;
    }

    /**Permette di specificare/modificare la  a cui appartiene l'attuatore
     * @param categoria categoria di attuatore da assegnare
     */
    public void setCategoria(CategoriaAttuatore categoria) {
        this.categoria = categoria;
    }


    /**Permette di ottenere la lista degli artefatti comandati dall'attuatore
     * @return la lista degli artefatti comandati dall'attuatore
     */
    public ArrayList<Artefatto> getListaComandati() {
        return listaComandati;
    }

    /**Aggiunge l'artefatto alla lista degli artefatti comandati dall'attuatore
     * @param art nuovo artefatto che si desidera comandare attraverso l'attuatore
     */
    public void aggiungiArtefatto(Artefatto art) {
        listaComandati.add(art);
        for (ModalitaOperativa m: this.categoria.getModalita()) {
            if (m.getValore().equals(modalitaAttuale)) {
                art.setStatoAttuale(m);
                break;
            }
        }
    }

    /**Permette di specificare la lista di artefatti che si desiderano comandare attraverso l'attuatore
     * @param listaComandati nuova lista di artefatti comandati dall'attuatore
     */
    public void setListaComandati(ArrayList<Artefatto> listaComandati) {
        this.listaComandati = listaComandati;
    }

    /**Fornisce il nome dell' attuale modalit&agrave; operativa({@link #modalitaAttuale}) esibita dall'attuatore
     * @return nome della modalita operativa attuale
     */
    public String getModalitaAttuale() {
        return modalitaAttuale;
    }

    /** Specifica una nuova modalit&agrave; operativa per l'attuatore controllando prima
     * che questa sia possibile(definita nel suo set di modalit&agrave; operative).
     * Inoltre determina un cambiamento dello stato degli stati artefatti comandati
     *
     * @param nuovaModalita nome della nuova modalit&agrave; operativa
     */
    public void setModalitaAttuale(String nuovaModalita) {
        if(this.modalitaAttuale.equals(nuovaModalita)) {
            System.out.println("*** Sei già in questa modalità ***");
            return;
        }
        //Giusto perche l'utente lo sappia
        // TODO: 10/04/2020 aggiungere in v3 e v4
        if (this.listaComandati.isEmpty()) {
            System.out.println("--- L'attuatore non comanda alcun artefatto! ---");
        }
        for (ModalitaOperativa mod : this.getCategoria().getModalita()) {
            if(mod.getValore().equals(nuovaModalita)) {
                this.modalitaAttuale = nuovaModalita;
                modificaArtefatti(new ModalitaOperativa(nuovaModalita)); //conseguente cambiamento di stato degli artefatti comandati

                System.out.println("*** Modalità modificata correttamente ***");
                return;
            }
        }
        System.out.println("!!! Questa modalità non esiste per questo attuatore !!!");
    }

    /** Specifica una nuova modalit&agrave; operativa per l'attuatore controllando prima
     * che questa sia possibile(definita nel suo set di modalit&agrave; operative).
     * Inoltre determina un cambiamento dello stato degli stati artefatti comandati
     *
     * @param nuovaModalita nome della nuova modalit&agrave; operativa
     * @param nomeParametro nome del nuovo paramentro da settare
     * @param valoreParametro setta il nuovo valore della modalità paramentrica inserita
     */
    public void setModalitaAttuale(String nuovaModalita, String nomeParametro, int valoreParametro) {
        if(this.modalitaAttuale.equals(nuovaModalita)) {
            System.out.println("+ Sei già in questa modalità +");
        }
        //Giusto perche l'utente lo sappia (piu importante qua per il problema citato nel DaFarsi sopra citato)
        // TODO: 10/04/2020 aggiugere in v3,v4
        if(this.listaComandati.isEmpty()) {
            System.out.println("--- L'attuatore non comanda alcun artefatto! ---");
        }

        //TODO: Implementare magari con un metodo getModalita che usa clone per cateAtt come cateSens(Non per Mattia)
        for (ModalitaOperativa mod : this.getCategoria().getModalita()) {
            if(mod.getValore().equals(nuovaModalita)) {
                this.modalitaAttuale = nuovaModalita;

                /*HashMap<String, Integer> nuoviParam = new HashMap<>();
                HashMap<String,Integer> vecchiParam = mod.getParametri();

                for (String key : vecchiParam.keySet()) {
                    nuoviParam.put(key,vecchiParam.get(key));
                }
                ModalitaOperativa nuovaMod = new ModalitaOperativa(nuovaModalita, nuoviParam);
                 */
                //TODO: Sostituire al posto di parte sopra in v3,v4
                try {
                    ModalitaOperativa nuovaMod = (ModalitaOperativa) mod.clone();
                    nuovaMod.setParametro(nomeParametro, valoreParametro);
                    modificaArtefatti(nuovaMod);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                System.out.println("*** Modalità modificata correttamente ***");
                return;
            }
        }
        System.out.println("Questa modalità non esiste per questo attuatore");
    }

    /** Modifica la modalit&agrave; operatica/stato degli artefatti comandati dall'attuatore
     * @param mod nuova modalit&agrave; operativa da assegnare agli artefatti comandati dall'attuatore
     */

    private void modificaArtefatti(ModalitaOperativa mod) {
        for (Artefatto art: listaComandati) {
            art.setStatoAttuale(mod);
        }
    }

    /**Fornisce lo stato di attivazione dell'attuatore(accesso o spento)
     * @return true se l'attuatore è acceso e false se è spento
     */
    public boolean isAttivo() {
        return statoAttivazione;
    }

    /** Permette di 'attivare' il dispositivo o specificare se è attivo o spento
     * @param statoAttivazione nuovo stato dell'attuatore(true=Acceso, false=Spento)
     */
    public void setStatoAttivazione(boolean statoAttivazione) {
        this.statoAttivazione = statoAttivazione;
    }

    public boolean isSingolo() {
        return singolo;
    }
}
