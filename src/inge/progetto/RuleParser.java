package inge.progetto;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static inge.progetto.Main.getOraCorrente;

/**
 * Classe di utility usata per le varie operazioni di gestione delle regole, che esse vengano importate o generate interattivamente da un utente.
 * Si occupa di salvataggio permanente delle regole o di ripristino/lettura da file per l'utente che interagisce con i vari componenti del sistema come unita immobiliari o dispositivi
 * come sensori o attuatori . Inoltre è possibile anche importare infatti regole da file esterni purchè queste siano compatibili con la descrizione
 * precedentemente fornita del sistema e che rispettino la grammatica e sintassi del sistema.
 * Svolge appunto la funzione di parsing, ovvero traduce il formato testuale delle regole assegnate/inserite dall'utente in comandi o azioni con le
 * quali modifica la configurazione di una determinata unita immobiliare agendo su singoli sensori e attuatori in essa presenti.
 *
 * @author Parampal Singh, Mattia Nodari
 */
public class RuleParser {

    /**
     * percorso del file con cui lavora il rule parser
     */
    private String fileName;

    /**
     * timer usato per azioni programmate nel tempo
     * @see Timer
     */
    private MyTimer timer;

    /**
     * Costruttore per un'istanza RuleParser
     */
    public RuleParser() {
        this.fileName = "";
    }

    /**Permette di specificare il percorso del file da cui estrarre le regole
     * @param fileName percorso del file
     */
    public void setUp(String fileName) {
        this.fileName = fileName;
        this.timer = new MyTimer("TimerThread");
    }

    /**
     * Cancella tutte le attività precedentemente programmate se presenti
     */
    public void stopTimer() {
        if(timer != null)
            this.timer.cancel();
    }

    /**Permette la scrittura di regola/e nel file dal percorso specficato in modo da salvare permanentemente
     * @param text regola/e che si desiderano salvare
     * @param append flag per decidere se sovrascivere il file o scrivere in coda
     */
    public void writeRuleToFile(String text, boolean append) {
        if (fileName.isEmpty())
            return;

        try {
            FileWriter fileWriter = new FileWriter(fileName, append);
            PrintWriter writer = new PrintWriter(fileWriter);

            writer.println(text);

            writer.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Viene effettuata la lettura da file per recuperare la lista delle regole create dal fruitore.
     * @return la lettura delle regole dal file dell'unità immobiliare corrente
     */
    public String readRuleFromFile() {
        return readFromFile(this.fileName);
    }

    /**Legge da file con percorso specificato
     * @param fileName percorso del file da cui leggere
     * @return lettura dal file specificato
     */
    public static String readFromFile(String fileName) {

        StringBuilder output = new StringBuilder();
        boolean presente = new File(fileName).exists();

        if (!presente) {
            return "";
        }
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            bufferedReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    /**Elimina eventuali regole doppie e verifica e cambia se necessario l'abilitazione delle regole salvate
     * @param listaSensori lista di sensori con cui verificare l'abilitazione della regola
     * @param listaAttuatori lista di attuatori con cui verificare l'abilitazione della regola
     */
    public void eliminaDoppie(ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        String lettura = readRuleFromFile();

        if (lettura.equals(""))
            return;

        String[] regole = lettura.split("\n");


        ArrayList<String> regoleList = new ArrayList<>(Arrays.asList(regole));
        ArrayList<String> regoleSenzaAbil = new ArrayList<>();

        for (String reg : regoleList) {
            regoleSenzaAbil.add(reg.replace("ENABLED --> IF ","").replace("DISABLED --> IF ",""));
        }

        List<String> listWithoutDuplicates = regoleSenzaAbil.stream().distinct().collect(Collectors.toList());
        StringBuilder regoleModificate = new StringBuilder();

        for (int i = 0; i < listWithoutDuplicates.size() - 1; i++) {

            if (verificaAbilitazione(listWithoutDuplicates.get(i),listaSensori,listaAttuatori))
                regoleModificate.append("ENABLED --> IF ");
            else
                regoleModificate.append("DISABLED --> IF ");

            regoleModificate.append(listWithoutDuplicates.get(i)).append("\n");
        }

        if (verificaAbilitazione(listWithoutDuplicates.get(listWithoutDuplicates.size() - 1),listaSensori,listaAttuatori))
            regoleModificate.append("ENABLED --> IF ");
        else
            regoleModificate.append("DISABLED --> IF ");

        regoleModificate.append(listWithoutDuplicates.get(listWithoutDuplicates.size() - 1));
        writeRuleToFile(regoleModificate.toString(), false);
    }

    /**
     * Importa regole da un file esterno specificatp dall'utente, solo quelle effettivamente compatibili con
     * le liste di dispositivi assegnati
     * @param file percorso del file da cui importare
     * @param listaSensori lista di sensori con cui verificare compatibilita della regola
     * @param listaAttuatori lista di attuatori con cui verificare compatibilita della regola
     */
    public void importaRegole(String file, ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        ArrayList<String> nomiDispPres = new ArrayList<>();

        for (Sensore s : listaSensori) {
            nomiDispPres.add(s.getNome());
        }

        for (Attuatore att : listaAttuatori) {
            nomiDispPres.add(att.getNome());
        }

        String regoleImport = readFromFile(file);

        if (regoleImport.equals("")) {
            System.out.println("XX Errore di lettura file. Non sono state inserite regole per l'unita immobiliare XX\n");
            return;
        }

        String[] regole = regoleImport.split("\n");

        for (String r : regole) {
            try {
                ArrayList<String> dispTrovati = verificaCompRegola(r);

                for (String nomeDis : dispTrovati) {
                    if (nomeDis.contains(".")){
                        String nomeS = nomeDis.split("\\.")[0];

                        if (!nomiDispPres.contains(nomeS)) {
                            throw new Exception("XX Dispositivi Incompatibili all'interno della regola XX\n");
                        }

                        Sensore s = listaSensori.stream().filter(sensore -> sensore.getNome().equals(nomeS)).iterator().next();
                        String nomeInfo = nomeDis.split("\\.")[1];

                        if (s.getInformazione(nomeInfo) == null)
                            throw new Exception("XX Regola non compatibile XX\n");


                    } else if (!nomiDispPres.contains(nomeDis)) {
                        throw new Exception("XX Dispositivi Incompatibili all'interno della regola XX\n");

                    }
                }
                System.out.println("*** Importazione della regola avvenuta con successo ***\n");
                writeRuleToFile(r, true);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
        eliminaDoppie(listaSensori,listaAttuatori);
    }

    /** Verifica la compatibilità sintattica della regola e ne restituisce una lista di nomi dei dispositivi in essa presenti
     * @param regola regola da controllare
     * @return lista di nomi dei dispositivi presenti nella regola
     * @throws Exception in caso di errore sintattico nella regola
     */
    protected ArrayList<String> verificaCompRegola(String regola) throws Exception {

        String r2 = regola.replace("ENABLED --> IF ", "").replace("DISABLED --> IF ", "");
        String[] tokens = r2.split(" THEN ");

        ArrayList<String> nomiDisp = new ArrayList<>();

        String[] condizioni = tokens[0].split("( AND | OR )");

        for (String cond : condizioni) {

            if (cond.equals("true"))
                continue;

            if (!cond.matches("[^<>=\t\n ]+ ([<>=]|<=|>=) [^<>=\t\n ]+"))
                throw new Exception("XX Regola non compatibile XX\n");

            String[] operandi = cond.split(" ([><=]|>=|<=) ");

            if (operandi[0].matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+\\.([a-zA-Z0-9])+(_[A-Za-z][a-zA-Z0-9]*)*"))
                nomiDisp.add(operandi[0]);

            else if (operandi[0].equals("time"))
                continue;
            else
                throw new Exception("XX Regola non compatibile XX\n");

            if (operandi[1].matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+\\.([a-zA-Z0-9])+(_[A-Za-z][a-zA-Z0-9]*)*"))
                nomiDisp.add(operandi[1]);

            else if (!operandi[1].matches("-?[0-9]+") && !operandi[1].matches("([0-1]?[0-9]|2[0-3])(\\.)[0-5]?[0-9]") && !operandi[1].matches("[A-Za-z]+"))
                throw new Exception("XX Regola non compatibile XX\n");
        }

        String[] azioni = tokens[1].split(" ; ");
        for (String az : azioni) {

            if (az.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+ := [a-zA-Z0-9]+")) {
                nomiDisp.add(az.split(" := ")[0]);

            } else if (az.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+ := [a-zA-Z0-9]+ , start := ([0-1]?[0-9]|2[0-3])(\\.)[0-5]?[0-9]")) {
                nomiDisp.add(az.split(" ")[0]);
            } else if (az.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+ := [a-zA-Z0-9]+\\|[a-zA-Z0-9]+\\|-?[0-9]+")) {
                nomiDisp.add(az.split(" ")[0]);

            } else {
                throw new Exception("XX Regola non compatibile XX\n");
            }
        }

        return nomiDisp;
    }

    /**
     * Il metodo applica le regole presenti nella sezione conseguente di una regola quando l'antecedente risulta true.
     *
     * @param listaSensori   dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     * @param listaAttuatori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     */
    public void applyRules(ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        String readRules = this.readRuleFromFile();

        System.out.println("\n\n...APPLICAZIONE REGOLE...\n");

        if (readRules.equals("")) {
            System.out.println("XX Non sono state inserite regole per l'unita immobiliare XX\n");
            return;
        }

        String[] rules = readRules.split("\n");

        for (String r : rules) {
            if (!verificaAbilitazione(r, listaSensori, listaAttuatori) || r.startsWith("DISABLED -->"))
                continue;

            String r2 = r.replace("ENABLED --> IF ", "");
            String[] tokens = r2.split(" THEN ");

            boolean ris = calculate(tokens[0], listaSensori);
            System.out.println(r2 + " :: " + ris);
            if (ris)
                applyActions(tokens[1], listaAttuatori);
        }
    }

    /** Verifica che la regola possa essere considerata abilitata o meno a seconda che abbia tutti dispositivi(attuatori e sensori) attivi
     * @param regola regoal da verificare
     * @param listaSensori lista di sensori con cui verificare l'abilitazione della regola
     * @param listaAttuatori lista di sensori con cui verificare l'abilitazione della regola
     * @return true se la regola è abilitata, false altrimenti
     */
    protected boolean verificaAbilitazione(String regola, ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        ArrayList<String> nomiDisp;
        try {
            nomiDisp = verificaCompRegola(regola);
        } catch (Exception e) {
            return false;
        }

        for (Sensore sens : listaSensori) {
            if (!sens.isAttivo() && nomiDisp.contains(sens.getNome())) {
                return false;
            }
        }

        for (Attuatore att : listaAttuatori) {
            if (!att.isAttivo() && nomiDisp.contains(att.getNome())) {
                return false;
            }
        }
        return true;
    }

    /**Permette di cambiare l'abilitazione della regola specificata
     * @param target regola da abilitare o meno
     * @param abil abilitazione (false = disabilitata, true = abilitata)
     */
    public void cambiaAbilitazioneRegola(String target, boolean abil) {
        String[] letto = readRuleFromFile().split("\n");

        for (int i = 0; i < letto.length; i++) {
            if (letto[i].equals(target)) {
                if (abil) {
                    if (letto[i].startsWith("DISABLED --> "))
                        letto[i] = letto[i].replace("DISABLED --> ", "ENABLED --> ");

                } else {

                    if (letto[i].startsWith("ENABLED --> "))
                        letto[i] = letto[i].replace("ENABLED --> ", "DISABLED --> ");

                }
                break;
            }
        }
        String regoleModificate = "";
        for (int i = 0; i < letto.length - 1; i++) {
            regoleModificate += letto[i] + "\n";
        }
        regoleModificate += letto[letto.length - 1];
        writeRuleToFile(regoleModificate, false);
    }

    /** Abilita regole che contengono il dispositivo dal nome specificato
     * @param nomeDispositivo nome del dispositivo abilitante
     * @param listaSensori lista di sensori con cui verificare l'abilitazione della regola
     * @param listaAttuatori lista di sensori con cui verificare l'abilitazione della regola
     */
    public void abilitaRegoleconDispositivo(String nomeDispositivo, ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        String[] regole = readRuleFromFile().split("\n");

        for (String s : regole) {
            try {
                ArrayList<String> disps = verificaCompRegola(s);

                if (disps.contains(nomeDispositivo)) {
                    cambiaAbilitazioneRegola(s,false);
                }

            } catch (Exception e) {
                String msg = e.getMessage();
            }

            if (s.contains(nomeDispositivo) && verificaAbilitazione(s,listaSensori,listaAttuatori)) {
                cambiaAbilitazioneRegola(s, true);
            }
        }
    }


    /**
     * Disabilita regole che contengono il dispositivo dal nome specificato
     * @param nomeDispositivo nome del dispositivo disabilitante
     */
    public void disabilitaRegolaConDispositivo(String nomeDispositivo) {
        String[] regole = readRuleFromFile().split("\n");
        for (String s : regole) {
            try {
                ArrayList<String> disps = verificaCompRegola(s);

                if (disps.contains(nomeDispositivo)) {
                    cambiaAbilitazioneRegola(s,false);
                }

            } catch (Exception e) {
                String msg = e.getMessage();
            }

            if (s.contains(nomeDispositivo)) {
                cambiaAbilitazioneRegola(s, false);
            }
        }
    }

    /**
     * Il metodo isola una singola azione e la passa al metodo che applica le singole regole.
     *
     * @param token          sezione di azione da eseguire
     * @param listaAttuatori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     */
    private void applyActions(String token, ArrayList<Attuatore> listaAttuatori) {
        for (String tok : token.split(" ; "))
            if (tok.contains("start")) {
                Date data = getTime(tok.split(" , ")[1].split(" := ")[1]);
                this.timer.schedule(new AzioneProgrammata(listaAttuatori, tok.split(" , ")[0]), data);

            } else {
                apply(tok, listaAttuatori);
            }
    }

    /**Permette di ottenere ora da formato testuale hh.mm
     * @param time formato testuale dell'ora
     * @return istanza di {@link Date} che si puo utilizzare per programmare azioni
     */
    public static Date getTime(String time) {
        String[] timetokens = time.split("\\.");
        int hour = Integer.parseInt(timetokens[0]);
        int minute = Integer.parseInt(timetokens[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Il metodo applica la regola vera e propria, settando il nuovo valoro della modalità operativa dell'attuatore.
     *
     * @param act            azione da effettuare.
     * @param listaAttuatori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     */
    private synchronized void apply(String act, ArrayList<Attuatore> listaAttuatori) {
        String[] toks = act.split(" := ");

        Attuatore actD = null;
        for (Attuatore att : listaAttuatori) {
            if (att.getNome().equals(toks[0])) {
                actD = att;
            }
        }

        if (actD == null)
            return;

        String modPrecedente = actD.getNome() + ": " + actD.getModalitaAttuale() + " -> ";
        if (toks[1].contains("|")) {
            String[] againTokens = toks[1].split("\\|");

            if (!againTokens[2].matches("-?[0-9]+"))
                return;

            actD.setModalitaAttuale(againTokens[0], againTokens[1], Integer.parseInt(againTokens[2]));
        } else {
            actD.setModalitaAttuale(toks[1]);
        }
        System.out.println(modPrecedente + actD.getModalitaAttuale() + "\n");

    }

    /**
     * Il metodo utilizza gli operatori logici per separare la stringa delle condizioni e verificare singolarmente le varie operazioni e poi applicare
     * gli operatori logici di AND e OR.
     *
     * @param cos          è la condizione affinchè una regola si verifichi.
     * @param listaSensori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     * @return il risultato di una determinata espressione booleana/antecedente
     */
    private boolean calculate(String cos, ArrayList<Sensore> listaSensori) {
        if (cos.equals("true"))
            return true;

        if (cos.contains("OR")) {
            String[] expTok = cos.split(" OR ", 2);
            return calculate(expTok[0], listaSensori) || calculate(expTok[1], listaSensori);
        }

        if (cos.contains("AND")) {
            String[] expTok = cos.split(" AND ", 2);
            return calculate(expTok[0], listaSensori) && calculate(expTok[1], listaSensori);
        }
        if (cos.matches("time ([<>=]|<=|>=) ([0-1]?[0-9]|2[0-3])(\\.)[0-5]?[0-9]")) {
            return evalTimeExp(cos.split(" "));
        }

        if (cos.matches("[^<>=\t\n ]+ ([<>=]|<=|>=) [^<>=\t\n ]+")) {
            String[] expTok = cos.split(" ");
            return getValExp(expTok, listaSensori);

        }
        return false;

    }

    /**Valuta una disuglianza temporale, un confronto tra misure temporale: time(valore corrente) op(operatore) time2(istante temporale specificato)
     * @param expTok disuguaglianza temporale da valutare
     * @return il risultato della disuguaglianza
     */
    private boolean evalTimeExp(String[] expTok) {
        Date currentDate = Calendar.getInstance().getTime();
        Date confDate = getTime(expTok[2]);
        String operator = expTok[1];

        if (operator.startsWith("<")) {
            if (operator.endsWith("="))
                return currentDate.compareTo(confDate) <= 0;
            else
                return currentDate.compareTo(confDate) < 0;

        } else if (operator.startsWith(">")) {
            if (operator.endsWith("="))
                return currentDate.compareTo(confDate) >= 0;
            else
                return currentDate.compareTo(confDate) > 0;

        } else {
            return currentDate.compareTo(confDate) == 0;
        }

    }

    /**
     * Il metodo viene usato per acquisire i valori dei sensori in gioco e per confrontare l'effettiva operazione tra due sensori o tra un sensore e un valore numerico costante
     * o una stringa nel caso di un'informazione non numerica
     *
     * @param listaSensori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     * @param toks componenti dell'espressione da valutare
     * @return il risultato dell'operazione in termini di true se le operazioni sono verificate altrimenti false se sono false
     */
    private boolean getValExp(String[] toks, ArrayList<Sensore> listaSensori) {
        String var1 = toks[0];
        String operator = toks[1];
        String var2 = toks[2];


        String[] sensVar = var1.split("\\.");
        Sensore sens1 = null;

        for (Sensore s : listaSensori) {
            if (s.getNome().equals(sensVar[0])) {
                sens1 = s;
                break;
            }
        }

        if (sens1 == null)
            return false;

        Informazione misura1 = sens1.getInformazione(sensVar[1]);

        if (misura1 == null) {
            return false;
        }

        if (var2.matches("-?[0-9]+")) {

            if (misura1.getTipo().equals("NN"))
                return false;

            int value = (int) misura1.getValore();
            int num = Integer.parseInt(var2);

            System.out.println();

            return evalOp(operator, value, num);
        }

        if (var2.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+\\.([a-zA-Z0-9])+(_[A-Za-z][a-zA-Z0-9]*)*")) {
            String[] sensVar2 = var2.split("\\.");
            Sensore sens2 = null;

            for (Sensore s : listaSensori) {
                if (s.getNome().equals(sensVar2[0])) {
                    sens2 = s;
                    break;
                }
            }

            if (sens2 == null)
                return false;

            Informazione misura2 = sens2.getInformazione(sensVar2[1]);

            if (misura2 == null)
                return false;

            if (!misura1.getTipo().equals(misura2.getTipo()))
                return false;

            if (misura1.getTipo().equals("NN")) {
                String sca1 = (String) misura1.getValore();
                String sca2 = (String) misura2.getValore();

                if (!operator.equals("="))
                    return false;
                else
                    return sca1.equals(sca2);

            } else {
                int value1 = (int) misura1.getValore();
                int value2 = (int) misura2.getValore();

                return evalOp(operator, value1, value2);
            }
        }

        if (var2.matches("[a-zA-Z]+") && operator.equals("=")) {
            if (misura1.getTipo().equals("NN")) {
                String sca1 = (String) misura1.getValore();
                return var2.equals(sca1);
            }
        }

        return false;
    }

    /**
     * Il metodo effettua il risultato booleano dell'operazione logica tra i due valori con l'operatore designato.
     *
     * @param operator è l'operatore per il confronto della regola
     * @param value1   valore di sx dell'operazione
     * @param value2   valore di dx dell'operazione
     * @return il confronto dell' operazione
     */
    private boolean evalOp(String operator, int value1, int value2) {
        if (operator.startsWith("<")) {
            if (operator.endsWith("="))
                return value1 <= value2;
            else
                return value1 < value2;

        } else if (operator.startsWith(">")) {
            if (operator.endsWith("="))
                return value1 >= value2;
            else
                return value1 > value2;

        } else {
            return value1 == value2;

        }
    }

    /**
     * Classe innestata utilizzata per definire un'azione programmata in un istante temporale futuro specificato all'interno di una regola.
     * Ad esempio, l’azione (a1_attLuciEsterne := spegnimento, start := 6.00) assegna la modalità di spegnimento all’attuatore a1 della
     * categoria attLuciEsterne esattamente alle ore 6.00. Il sistema 'mette in coda' tale azione e la esegue esattamente all'ora specificata
     */
    public class AzioneProgrammata extends TimerTask {

        /**
         * Attuatori con cui interagisce l'azione
         */
        private final ArrayList<Attuatore> attuatori;

        /**
         * azione da eseguire specificata dall'utente
         */
        private final String azione;

        /**Costruttore di un'istanza Azione Programmata
         * @param attuatori attuatori su cui agisce l'azione/assegnamentp
         * @param azione azione da eseguire
         */
        public AzioneProgrammata(ArrayList<Attuatore> attuatori, String azione) {
            this.attuatori = attuatori;
            this.azione = azione;
        }

        /**Permette di ottenere in formato avviamente testuale l'azione da eseguire specificata dall'utente
         * @return azione da eseguire
         */
        public String getAzione() {
            return azione;
        }

        @Override
        public synchronized void run() {
            System.out.println("\n\n...AZIONE PROGRAMMATA ORARIO " + getOraCorrente() + "...");
            apply(this.azione, this.attuatori);
            timer.eliminaTask(azione);
        }
    }
}
