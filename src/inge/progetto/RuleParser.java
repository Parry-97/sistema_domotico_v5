package inge.progetto;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static inge.progetto.Main.getOraCorrente;

public class RuleParser {

    private String fileName;
    private MyTimer timer;

    public RuleParser() {
        this.fileName = "";
    }

    // TODO: 09/04/2020 Perche avevo in mente di usare le liste di dispositivi?(Non per Mattia)
    public void setUp(String fileName) {
        this.fileName = fileName;
        this.timer = new MyTimer("TimerThread");
    }

    public void stopTimer() {
        if(timer != null)
            this.timer.cancel();
    }

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
     *
     * @return la lettura delle regole dal file dell'unità immobiliare corrente
     */
    public String readRuleFromFile() {
        return readFromFile(this.fileName);
    }

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

    public void eliminaDoppie() {
        String[] regole = readRuleFromFile().split("\n");
        ArrayList<String> regoleList = new ArrayList<>(Arrays.asList(regole));

        List<String> listWithoutDuplicates = regoleList.stream().distinct().collect(Collectors.toList());
        String regoleModificate = "";
        for (int i = 0; i < listWithoutDuplicates.size() - 1; i++) {
            regoleModificate += listWithoutDuplicates.get(i) + "\n";
        }
        regoleModificate += listWithoutDuplicates.get(listWithoutDuplicates.size() - 1);
        writeRuleToFile(regoleModificate, false);
    }

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
                    if (!nomiDispPres.contains(nomeDis)) {
                        throw new Exception("!!! Dispositivi nella regola non sono disponibili per quest'unita immobiliare \n!!!");
                    }
                }

                writeRuleToFile(r, true);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
        eliminaDoppie();
    }

    protected ArrayList<String> verificaCompRegola(String regola) throws Exception {
        // TODO: 12/04/2020 Fare output decente

        String r2 = regola.replace("ENABLED --> IF ","").replace("DISABLED --> IF ","");
        String[] tokens = r2.split(" THEN ");

        ArrayList<String> nomiDisp = new ArrayList<>();

        String[] condizioni = tokens[0].split("( AND | OR )");

        for (String cond : condizioni) {

            if (cond.equals("true"))
                continue;

            if (!cond.matches("[^<>=\t\n ]+ ([<>=]|<=|>=) [^<>=\t\n ]+"))
                throw new Exception("!!! REGOLA NON COMPATIBILE !!!\n");

            String[] operandi = cond.split(" ([><=]|>=|<=) ");

            if (operandi[0].matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+\\.([a-zA-Z0-9])+(_[A-Za-z][a-zA-Z0-9]*)*"))
                nomiDisp.add(operandi[0].split("\\.")[0]);

            else if (operandi[0].equals("time"))
                continue;
            else
                throw new Exception("!!! REGOLA NON COMPATIBILE !!!\n");

            if (operandi[1].matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+\\.([a-zA-Z0-9])+(_[A-Za-z][a-zA-Z0-9]*)*"))
                nomiDisp.add(operandi[1].split("\\.")[0]);

            else if (!operandi[1].matches("-?[0-9]+") && !operandi[1].matches("([0-1]?[0-9]|2[0-3])(\\.)[0-5]?[0-9]") && !operandi[1].matches("[A-Za-z]+"))
                throw new Exception("!!! REGOLA NON COMPATIBILE !!!\n");
        }

        String[] azioni = tokens[1].split(" ; ");
        for (String az : azioni) {

            if (az.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+ := [a-zA-Z0-9]+")) {
                nomiDisp.add(az.split(" := ")[0]);

            } else if (az.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+ := [a-zA-Z0-9]+ , start := ([0-1]?[0-9]|2[0-3])(\\.)[0-5]?[0-9]")) {
                nomiDisp.add(az.split(" ")[0]);
            } else {
                throw new Exception("!!! REGOLA NON COMPATIBILE !!!");
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

    // TODO: 11/04/2020 magari fare un pattern per il nome del sensore
    //      Cosi è fin troppo stupido credo
    protected boolean verificaAbilitazione(String regola, ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        for (Sensore sens : listaSensori) {
            if (!sens.isAttivo() && regola.contains(sens.getNome() + ".")) {
                return false;
            }
        }

        for (Attuatore att : listaAttuatori) {
            if (!att.isAttivo() && regola.contains(att.getNome())) {
                return false;
            }
        }
        return true;
    }

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

    public void abilitaRegoleconDispositivo(String nomeDispositivo, ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        String[] regole = readRuleFromFile().split("\n");
        for (String s : regole) {
            if (s.contains(nomeDispositivo)) {
                if (verificaAbilitazione(s, listaSensori, listaAttuatori)) {
                    cambiaAbilitazioneRegola(s, true);
                }
            }
        }
    }

    // TODO: 11/04/2020 magari fare un pattern per il nome del dispositivo
    public void disabilitaRegolaConDispositivo(String nomeDispositivo) {
        String[] regole = readRuleFromFile().split("\n");
        for (String s : regole) {
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

        // TODO: 11/04/2020 migliorare magari pattern qua?
        if (cos.matches("[^<>=\t\n ]+ ([<>=]|<=|>=) [^<>=\t\n ]+")) {
            String[] expTok = cos.split(" ");
            return getValExp(expTok, listaSensori);

        }
        return false;

    }

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
     * o un astringa nel caso di un'informazione non numerica
     *
     * @param listaSensori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
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
        // TODO: 11/04/2020 magari semplificare un'po qua..magari salvarlo come Pattern
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

    public class AzioneProgrammata extends TimerTask {

        private final ArrayList<Attuatore> attuatori;
        private final String azione;

        public AzioneProgrammata(ArrayList<Attuatore> attuatori, String azione) {
            System.out.println(Thread.currentThread().getName());
            this.attuatori = attuatori;
            this.azione = azione;
        }

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
