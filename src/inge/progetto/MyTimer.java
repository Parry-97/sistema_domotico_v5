package inge.progetto;

import java.util.*;

public class MyTimer extends Timer {
    private final HashMap<Date, String> azioniProgrammate;

    public MyTimer(String name) {
        super(name);
        this.azioniProgrammate = new HashMap<>();
    }

    //TODO: Testare con esecuzione codice
    public void schedule(RuleParser.AzioneProgrammata task, Date time) {

        if (time.compareTo(Calendar.getInstance().getTime()) < 0)
            return;

        String azione = task.getAzione();

        if (!azioniProgrammate.containsValue(azione)) {
            azioniProgrammate.put(time,azione);
            super.schedule(task, time);

        } else {
            for (Map.Entry<Date, String> entry : azioniProgrammate.entrySet()) {
                if (entry.getValue().equals(azione) && entry.getKey().compareTo(time) != 0) {
                    super.schedule(task,time);
                    azioniProgrammate.put(time,azione);
                    break;
                }

            }
        }
    }

    public void eliminaTask(String azione) {
        for (Map.Entry<Date, String> entry : azioniProgrammate.entrySet()) {
            if (entry.getValue().equals(azione)) {
                azioniProgrammate.remove(entry.getKey());
                break;
            }

        }
    }
}
