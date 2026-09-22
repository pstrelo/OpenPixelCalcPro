# Original User Request

## Initial Request — 2026-09-22T17:57:09+02:00

Riprogettazione completa da zero della schermata Calcolatrice Avanzata/Scientifica in stile "Google Math Solver" (Material 3 Expressive Dark Theme con 4 Tab tematiche: Algebra, Trigonometria, Analisi/Calculus, Alfabeto). Include display con box superiore per formula e matrice soluzioni, tasto FORMAT in basso (Dec, Frac, Eng, DMS, Pol), separazione tra inserimento `=` e tasto di esecuzione `➤`, pulsante Grafico scorporato in alto con vista cartesiana interattiva (split-screen/fullscreen) e supporto multi-equazione tipo Desmos/GeoGebra.

Working directory: /Users/lorenzotrinati/Documents/antigravity/Calcolatrice-Pixel
Integrity mode: development

## Requirements

### R1. Layout e Palette Material 3 Expressive fedele a Google Math Solver
- Barra orizzontale a 4 Tab tematiche con indicatori cromatici distinti:
  1. **Algebra**: accento Lilla/Viola (`#A855F7`) per tasti funzione algebrici e pulsante `➤` viola.
  2. **Trigonometry**: accento Verde Menta (`#10B981`) per tasti goniometrici e pulsante `➤` verde.
  3. **Calculus**: accento Cremisi/Rosa (`#EC4899`) per derivate, integrali, limiti e pulsante `➤` cremisi.
  4. **Alphabet**: accento Pesca/Arancio (`#F97316`) per lettere e alfabeto greco e pulsante `➤` arancio.
- Tema sempre scuro (Dark Theme, sfondo `#1E212D`) con pulsanti sagomati a squircle/pillola ad alto contrasto.
- Riga inferiore di editing e navigazione: cursore a frecce `[ ← ]`, `[ → ]`, cancellazione `[ ⌫ DEL ]`, `[ AC ]`, tasto `[ FORMAT ]` e pulsante pillola prominente `[ ➤ ]` (Execute).

### R2. Separazione Funzionale tra Inserimento Uguaglianza ed Esecuzione
- Il tasto `=` inserisce il simbolo `=` nell'equazione consentendo la digitazione di uguaglianze e disequazioni senza attivare il calcolo automatico.
- Il calcolo o la risoluzione simbolica/numerica viene avviata esclusivamente alla pressione del tasto `➤`.

### R3. Tasto FORMAT in Basso (Conversione Istantanea del Risultato)
- Tasto dedicato `FORMAT` posizionato sulla riga inferiore (accanto ad AC e al pulsante di esecuzione) per commutare istantaneamente il formato del risultato tra:
  1. Decimale standard (DEC)
  2. Frazione esatta continua S<=>D (FRAC)
  3. Notazione ingegneristica con esponenti multipli di 3 (ENG)
  4. Sessagesimale Gradi, Primi, Secondi (DMS)
  5. Coordinate Polari vettoriali (POL)

### R4. Display Multi-Equazione Stile Desmos/GeoGebra con Matrice Soluzioni
- Box di input superiore in stile Google Math Solver con supporto multi-riga tramite pulsante "+ Aggiungi equazione".
- Ogni equazione possiede un badge di colore identificativo abbinato alla rispettiva curva nel grafico.
- Risultato dell'equazione mostrato all'interno del box con notazione a staffa verticale [ x = -3 ; x = 3 ] per radici multiple.

### R5. Grafico Cartesiano 2D Scorporato in Alto
- Pulsante dedicato del grafico posizionato in alto, separato dalla tastiera (accanto alla casella di input).
- Consente la visualizzazione interattiva 2D (split-screen o fullscreen) con pan, zoom, tracciamento radici, intercette e aree di disequazione.

## Acceptance Criteria

### UI & Layout
- [ ] La barra delle 4 Tab permette di commutare tra Algebra, Trigonometry, Calculus e Alphabet, aggiornando dinamicamente la griglia dei tasti e il colore del tasto `➤`.
- [ ] La riga inferiore contiene `←`, `→`, `⌫`, `AC`, `FORMAT` e `➤`.
- [ ] Il pulsante del Grafico è posizionato in alto accanto al campo di input.
- [ ] Lo stile è conforme a Material 3 Expressive Dark Theme con contrasto elevato per schermi Pixel.

### Logica di Calcolo ed Esecuzione
- [ ] Premendo `=` viene aggiunto il carattere all'espressione senza valutare.
- [ ] Premendo `➤` viene eseguita la risoluzione (equazioni, disequazioni, calcoli aritmetici o analisi).
- [ ] Premendo `FORMAT` il risultato attivo cambia istantaneamente tra DEC, FRAC, ENG, DMS e POL.
- [ ] Le soluzioni di equazioni a più radici sono renderizzate con parentesi a matrice quadrata.

### Multi-Funzione e Grafici
- [ ] È possibile aggiungere e rimuovere equazioni secondarie con badge colorati distinti.
- [ ] Il grafico mostra le curve corrispondenti con supporto al tocco per visualizzare coordinate degli zeri `(x; y)`.
- [ ] Tutti i test unitari passano con successo e `./gradlew assembleDebug` compila senza errori.
