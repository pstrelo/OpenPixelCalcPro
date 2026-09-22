import os

OUTPUT_DIR = "/Users/lorenzotrinati/Documents/antigravity/Calcolatrice-Pixel/figma_export"
os.makedirs(OUTPUT_DIR, exist_ok=True)

W, H = 412, 892

# COLOR TOKENS (PIXEL MATERIAL 3 DARK THEME)
BG_COLOR = "#131418"
SURFACE_CARD = "#1E2025"
SURFACE_CARD_BORDER = "#2C2E35"
NUM_CONTAINER = "#26282E"
NUM_TEXT = "#E2E2E6"
OP_CONTAINER = "#373942"
OP_TEXT = "#A8C7FA"
CTRL_CONTAINER = "#373942"
CTRL_TEXT = "#E2E2E6"
EQUALS_CONTAINER = "#A8C7FA"
EQUALS_TEXT = "#041E49"
SCI_CONTAINER = "#2D2F36"
SCI_TEXT = "#C4C6D0"
TEXT_PRIMARY = "#E2E2E6"
TEXT_SECONDARY = "#8E9099"
TEXT_ACCENT = "#A8C7FA"
GREEN_ACCENT = "#34D399"
RED_ACCENT = "#FFB4AB"

FONT_FAMILY = "Roboto, -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif"

def status_bar(time="09:41"):
    return f"""
    <!-- Status Bar (Figma Native Vectors) -->
    <g id="StatusBar">
      <text x="36" y="38" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="14" font-weight="600">{time}</text>
      <!-- Signal Bars -->
      <g transform="translate(322, 26)" fill="{TEXT_PRIMARY}">
        <rect x="0" y="10" width="3" height="4" rx="0.5"/>
        <rect x="5" y="7" width="3" height="7" rx="0.5"/>
        <rect x="10" y="4" width="3" height="10" rx="0.5"/>
        <rect x="15" y="1" width="3" height="13" rx="0.5"/>
      </g>
      <!-- WiFi Icon -->
      <path d="M352 38 a 2 2 0 1 1 0.01 0 M347 34 a 8 8 0 0 1 10 0 M343 30 a 14 14 0 0 1 18 0" stroke="{TEXT_PRIMARY}" stroke-width="2" stroke-linecap="round" fill="none"/>
      <!-- Battery Icon -->
      <g transform="translate(366, 26)">
        <rect x="0" y="1" width="22" height="12" rx="3.5" fill="none" stroke="{TEXT_PRIMARY}" stroke-width="1.8"/>
        <rect x="2" y="3" width="14" height="8" rx="2" fill="{TEXT_PRIMARY}"/>
        <path d="M23 5 C23.8 5 24 5.5 24 7 C24 8.5 23.8 9 23 9" fill="{TEXT_PRIMARY}"/>
      </g>
    </g>
    <!-- Home Gesture Indicator -->
    <g id="HomeIndicator">
      <rect x="166" y="874" width="80" height="4" rx="2" fill="{TEXT_SECONDARY}" fill-opacity="0.6"/>
    </g>
    """

def top_bar(title="", back_btn=False, history_btn=False, menu_btn=True):
    items = []
    if back_btn:
        items.append(f"""
        <g id="BackBtn" cursor="pointer">
          <circle cx="44" cy="74" r="22" fill="transparent"/>
          <path d="M48 64 L38 74 L48 84" stroke="{TEXT_PRIMARY}" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        </g>""")
    if history_btn:
        items.append(f"""
        <g id="HistoryBtn" cursor="pointer">
          <rect x="20" y="52" width="44" height="44" rx="22" fill="{NUM_CONTAINER}"/>
          <path d="M43 65 A 7 7 0 1 0 49 72 M43 65 L43 61 M43 65 L47 65" stroke="{TEXT_PRIMARY}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          <path d="M42 70 L42 74 L45 76" stroke="{TEXT_PRIMARY}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        </g>""")
    if title:
        x_pos = 78 if back_btn else 32
        items.append(f"""
        <text x="{x_pos}" y="80" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="20" font-weight="600">{title}</text>
        """)
    if menu_btn:
        items.append(f"""
        <g id="MenuBtn" cursor="pointer">
          <circle cx="372" cy="74" r="22" fill="transparent"/>
          <circle cx="372" cy="66" r="2.2" fill="{TEXT_PRIMARY}"/>
          <circle cx="372" cy="74" r="2.2" fill="{TEXT_PRIMARY}"/>
          <circle cx="372" cy="82" r="2.2" fill="{TEXT_PRIMARY}"/>
        </g>""")
    return f"<g id='TopBar'>{''.join(items)}</g>"

def render_btn(x, y, w, h, rx, fill, text, text_color, font_size=24, font_weight="500"):
    # Calculate perfect vertical center for text baseline
    baseline_y = y + (h / 2) + (font_size * 0.35)
    center_x = x + (w / 2)
    return f"""
    <g id="Btn_{text}" cursor="pointer">
      <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" ry="{rx}" fill="{fill}"/>
      <text x="{center_x}" y="{baseline_y}" fill="{text_color}" font-family="{FONT_FAMILY}" font-size="{font_size}" font-weight="{font_weight}" text-anchor="middle" dominant-baseline="central">{text}</text>
    </g>"""

def render_backspace_btn(x, y, w, h, rx, fill, icon_color):
    center_x = x + (w / 2)
    center_y = y + (h / 2)
    # Standard clean Backspace path
    return f"""
    <g id="Btn_Backspace" cursor="pointer">
      <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" ry="{rx}" fill="{fill}"/>
      <g transform="translate({center_x - 12}, {center_y - 12})">
        <path d="M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 7.11c.36.53.9.89 1.59.89h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-3 12.59L17.59 17 14 13.41 10.41 17 9 15.59 12.59 12 9 8.41 10.41 7 14 10.59 17.59 7 19 8.41 15.41 12 19 15.59z" fill="{icon_color}"/>
      </g>
    </g>"""

# SCREEN 1: CALCOLATRICE PRINCIPALE
def screen_01():
    btn_w = 86
    btn_h = 68
    rx = 34
    spacing_x = 12
    spacing_y = 12
    start_x = 16
    start_y = 416

    buttons = []
    # Row 1: AC, ( ), %, ÷
    r1 = [
        ("AC", CTRL_CONTAINER, CTRL_TEXT, 22),
        ("( )", CTRL_CONTAINER, CTRL_TEXT, 22),
        ("%", CTRL_CONTAINER, CTRL_TEXT, 22),
        ("÷", OP_CONTAINER, OP_TEXT, 28)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r1):
        bx = start_x + col * (btn_w + spacing_x)
        buttons.append(render_btn(bx, start_y, btn_w, btn_h, rx, bg, lbl, fg, fs))

    # Row 2: 7, 8, 9, ×
    r2 = [
        ("7", NUM_CONTAINER, NUM_TEXT, 28),
        ("8", NUM_CONTAINER, NUM_TEXT, 28),
        ("9", NUM_CONTAINER, NUM_TEXT, 28),
        ("×", OP_CONTAINER, OP_TEXT, 28)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r2):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs, font_weight="400"))

    # Row 3: 4, 5, 6, −
    r3 = [
        ("4", NUM_CONTAINER, NUM_TEXT, 28),
        ("5", NUM_CONTAINER, NUM_TEXT, 28),
        ("6", NUM_CONTAINER, NUM_TEXT, 28),
        ("−", OP_CONTAINER, OP_TEXT, 28)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r3):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 2 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs, font_weight="400"))

    # Row 4: 1, 2, 3, +
    r4 = [
        ("1", NUM_CONTAINER, NUM_TEXT, 28),
        ("2", NUM_CONTAINER, NUM_TEXT, 28),
        ("3", NUM_CONTAINER, NUM_TEXT, 28),
        ("+", OP_CONTAINER, OP_TEXT, 28)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r4):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 3 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs, font_weight="400"))

    # Row 5: 0, ,, ⌫, =
    by5 = start_y + 4 * (btn_h + spacing_y)
    buttons.append(render_btn(start_x, by5, btn_w, btn_h, rx, NUM_CONTAINER, "0", NUM_TEXT, 28, font_weight="400"))
    buttons.append(render_btn(start_x + (btn_w + spacing_x), by5, btn_w, btn_h, rx, NUM_CONTAINER, ",", NUM_TEXT, 28, font_weight="400"))
    buttons.append(render_backspace_btn(start_x + 2 * (btn_w + spacing_x), by5, btn_w, btn_h, rx, NUM_CONTAINER, NUM_TEXT))
    buttons.append(render_btn(start_x + 3 * (btn_w + spacing_x), by5, btn_w, btn_h, rx, EQUALS_CONTAINER, "=", EQUALS_TEXT, 32, font_weight="600"))

    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(history_btn=True, menu_btn=True)}

  <!-- Display Section -->
  <g id="DisplayArea">
    <text x="380" y="240" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="28" font-weight="400" text-anchor="end">128 × 4.5</text>
    <text x="380" y="320" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="56" font-weight="400" text-anchor="end">576</text>
  </g>

  <!-- Scientific Toggle Handle (Down Chevron) -->
  <g id="ScientificExpandHandle" cursor="pointer">
    <circle cx="44" cy="382" r="18" fill="transparent"/>
    <path d="M38 380 L44 386 L50 380" stroke="{TEXT_SECONDARY}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
  </g>

  <!-- Keypad Container -->
  <g id="Keypad">
    {''.join(buttons)}
  </g>
</svg>"""

# SCREEN 2: CALCOLATRICE SCIENTIFICA (PRO MODE)
def screen_02():
    btn_w = 86
    btn_h = 50
    rx = 25
    spacing_x = 12
    spacing_y = 8
    start_x = 16
    start_y = 268

    buttons = []

    # Pro Sci Row 1: DEG, 2nd, x^y, ( )
    r1 = [
        ("DEG", "#384660", "#D3E3FD", 15),
        ("2nd", SCI_CONTAINER, SCI_TEXT, 15),
        ("xʸ", SCI_CONTAINER, SCI_TEXT, 16),
        ("( )", SCI_CONTAINER, SCI_TEXT, 16)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r1):
        bx = start_x + col * (btn_w + spacing_x)
        buttons.append(render_btn(bx, start_y, btn_w, btn_h, rx, bg, lbl, fg, fs))

    # Pro Sci Row 2: e, ln, log, √
    r2 = [
        ("e", SCI_CONTAINER, SCI_TEXT, 17),
        ("ln", SCI_CONTAINER, SCI_TEXT, 16),
        ("log", SCI_CONTAINER, SCI_TEXT, 16),
        ("√", SCI_CONTAINER, SCI_TEXT, 18)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r2):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs))

    # Pro Sci Row 3: sin, cos, tan, π
    r3 = [
        ("sin", SCI_CONTAINER, SCI_TEXT, 16),
        ("cos", SCI_CONTAINER, SCI_TEXT, 16),
        ("tan", SCI_CONTAINER, SCI_TEXT, 16),
        ("π", SCI_CONTAINER, SCI_TEXT, 18)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r3):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 2 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs))

    # Base Row 1: AC, !, %, ÷
    r4 = [
        ("AC", CTRL_CONTAINER, CTRL_TEXT, 18),
        ("!", CTRL_CONTAINER, CTRL_TEXT, 18),
        ("%", CTRL_CONTAINER, CTRL_TEXT, 18),
        ("÷", OP_CONTAINER, OP_TEXT, 24)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r4):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 3 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs))

    # Base Row 2: 7, 8, 9, ×
    r5 = [
        ("7", NUM_CONTAINER, NUM_TEXT, 22),
        ("8", NUM_CONTAINER, NUM_TEXT, 22),
        ("9", NUM_CONTAINER, NUM_TEXT, 22),
        ("×", OP_CONTAINER, OP_TEXT, 24)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r5):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 4 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs, font_weight="400"))

    # Base Row 3: 4, 5, 6, −
    r6 = [
        ("4", NUM_CONTAINER, NUM_TEXT, 22),
        ("5", NUM_CONTAINER, NUM_TEXT, 22),
        ("6", NUM_CONTAINER, NUM_TEXT, 22),
        ("−", OP_CONTAINER, OP_TEXT, 24)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r6):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 5 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs, font_weight="400"))

    # Base Row 4: 1, 2, 3, +
    r7 = [
        ("1", NUM_CONTAINER, NUM_TEXT, 22),
        ("2", NUM_CONTAINER, NUM_TEXT, 22),
        ("3", NUM_CONTAINER, NUM_TEXT, 22),
        ("+", OP_CONTAINER, OP_TEXT, 24)
    ]
    for col, (lbl, bg, fg, fs) in enumerate(r7):
        bx = start_x + col * (btn_w + spacing_x)
        by = start_y + 6 * (btn_h + spacing_y)
        buttons.append(render_btn(bx, by, btn_w, btn_h, rx, bg, lbl, fg, fs, font_weight="400"))

    # Base Row 5: 0, ,, ⌫, =
    by8 = start_y + 7 * (btn_h + spacing_y)
    buttons.append(render_btn(start_x, by8, btn_w, btn_h, rx, NUM_CONTAINER, "0", NUM_TEXT, 22, font_weight="400"))
    buttons.append(render_btn(start_x + (btn_w + spacing_x), by8, btn_w, btn_h, rx, NUM_CONTAINER, ",", NUM_TEXT, 22, font_weight="400"))
    buttons.append(render_backspace_btn(start_x + 2 * (btn_w + spacing_x), by8, btn_w, btn_h, rx, NUM_CONTAINER, NUM_TEXT))
    buttons.append(render_btn(start_x + 3 * (btn_w + spacing_x), by8, btn_w, btn_h, rx, EQUALS_CONTAINER, "=", EQUALS_TEXT, 26, font_weight="600"))

    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(history_btn=True, menu_btn=True)}

  <!-- Display Section -->
  <g id="DisplayArea">
    <text x="380" y="160" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="22" font-weight="400" text-anchor="end">sin(45°) × √2</text>
    <text x="380" y="215" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="44" font-weight="400" text-anchor="end">1</text>
  </g>

  <!-- Scientific Retract Handle (Up Chevron) -->
  <g id="ScientificRetractHandle" cursor="pointer">
    <circle cx="44" cy="245" r="18" fill="transparent"/>
    <path d="M38 248 L44 242 L50 248" stroke="{TEXT_SECONDARY}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
    <text x="68" y="249" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13" font-weight="600">DEG</text>
  </g>

  <!-- Pro Keypad (8 Rows) -->
  <g id="KeypadPro">
    {''.join(buttons)}
  </g>
</svg>"""

# SCREEN 3: MENU STRUMENTI / TOOLBOX
def screen_03():
    def card(y, icon, title, desc):
        return f"""
        <g id="Card_{title.replace(' ', '_')}" cursor="pointer">
          <rect x="20" y="{y}" width="372" height="74" rx="20" ry="20" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}" stroke-width="1"/>
          <rect x="34" y="{y + 14}" width="46" height="46" rx="14" ry="14" fill="#2B2F38"/>
          <text x="57" y="{y + 42}" font-size="22" text-anchor="middle" dominant-baseline="central">{icon}</text>
          <text x="94" y="{y + 34}" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="15" font-weight="600">{title}</text>
          <text x="94" y="{y + 54}" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="12" font-weight="400">{desc}</text>
          <path d="M366 {y + 32} L372 {y + 37} L366 {y + 42}" stroke="{TEXT_SECONDARY}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        </g>
        """

    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Strumenti e Funzioni", back_btn=True, menu_btn=False)}

  <!-- Section 1: Calcolo Avanzato -->
  <text x="24" y="130" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="700">CALCOLO AVANZATO</text>
  {card(145, "🧮", "Calcolatrice Avanzata", "Derivate, integrali, formule complesse")}
  {card(230, "📐", "Risolutore di Equazioni", "1°, 2° grado e sistemi a 2 incognite")}
  {card(315, "📈", "Grafici 2D di Funzioni", "Piano cartesiano interattivo con zoom/pan")}

  <!-- Section 2: Sensori e Utilità Pixel -->
  <text x="24" y="425" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="700">SENSORI &amp; STRUMENTI FISICI</text>
  {card(440, "🔄", "Convertitore di Unità", "Lunghezza, Peso, Valuta, Temperatura")}
  {card(525, "⚖️", "Livella a Bolla Digitale", "Inclinometro di precisione Pixel con sensori")}
  {card(610, "🧭", "Bussola Digitale", "Azimut, coordinate e direzione reale")}
  {card(695, "📏", "Righello su Schermo", "Calibrazione PPI per mm, cm e pollici")}
</svg>"""

# SCREEN 4: RISOLUTORE EQUAZIONI
def screen_04():
    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Risolutore Equazioni", back_btn=True, menu_btn=False)}

  <!-- Segmented Tabs -->
  <g id="Tabs" transform="translate(20, 115)">
    <rect x="0" y="0" width="372" height="44" rx="22" ry="22" fill="{SURFACE_CARD}"/>
    <text x="62" y="27" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13" font-weight="500" text-anchor="middle">1° Grado</text>
    
    <rect x="124" y="4" width="124" height="36" rx="18" ry="18" fill="{EQUALS_CONTAINER}"/>
    <text x="186" y="26" fill="{EQUALS_TEXT}" font-family="{FONT_FAMILY}" font-size="13" font-weight="600" text-anchor="middle">2° Grado</text>

    <text x="310" y="27" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13" font-weight="500" text-anchor="middle">Sistemi 2×2</text>
  </g>

  <!-- Formula Header Preview -->
  <g id="FormulaHeader">
    <rect x="20" y="180" width="372" height="60" rx="16" ry="16" fill="{NUM_CONTAINER}"/>
    <text x="206" y="216" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="20" font-weight="500" text-anchor="middle">ax² + bx + c = 0</text>
  </g>

  <!-- Input Fields (a, b, c) -->
  <g id="Inputs">
    <!-- a -->
    <rect x="20" y="260" width="112" height="62" rx="14" ry="14" fill="{SURFACE_CARD}" stroke="{OP_CONTAINER}" stroke-width="1.5"/>
    <text x="36" y="282" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="12" font-weight="600">a</text>
    <text x="36" y="306" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="18" font-weight="500">1</text>

    <!-- b -->
    <rect x="150" y="260" width="112" height="62" rx="14" ry="14" fill="{SURFACE_CARD}" stroke="{OP_CONTAINER}" stroke-width="1.5"/>
    <text x="166" y="282" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="12" font-weight="600">b</text>
    <text x="166" y="306" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="18" font-weight="500">-5</text>

    <!-- c -->
    <rect x="280" y="260" width="112" height="62" rx="14" ry="14" fill="{SURFACE_CARD}" stroke="{OP_CONTAINER}" stroke-width="1.5"/>
    <text x="296" y="282" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="12" font-weight="600">c</text>
    <text x="296" y="306" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="18" font-weight="500">6</text>
  </g>

  <!-- Solve Action Button -->
  <g id="SolveBtn" cursor="pointer">
    <rect x="20" y="340" width="372" height="52" rx="26" ry="26" fill="{TEXT_ACCENT}"/>
    <text x="206" y="372" fill="{EQUALS_TEXT}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600" text-anchor="middle">Risolvi Equazione</text>
  </g>

  <!-- Solutions & Detailed Steps -->
  <g id="SolutionCard">
    <rect x="20" y="415" width="372" height="390" rx="20" ry="20" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}" stroke-width="1"/>
    
    <text x="40" y="445" fill="{GREEN_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="700">DUE SOLUZIONI REALI DISTINTE</text>
    
    <!-- Solution Pills -->
    <rect x="40" y="465" width="158" height="64" rx="14" ry="14" fill="{NUM_CONTAINER}"/>
    <text x="60" y="490" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="12">Soluzione x₁</text>
    <text x="60" y="515" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="22" font-weight="600">x₁ = 3</text>

    <rect x="214" y="465" width="158" height="64" rx="14" ry="14" fill="{NUM_CONTAINER}"/>
    <text x="234" y="490" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="12">Soluzione x₂</text>
    <text x="234" y="515" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="22" font-weight="600">x₂ = 2</text>

    <!-- Resolution Steps -->
    <text x="40" y="560" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="600">Passaggi di Risoluzione:</text>
    <text x="40" y="590" fill="{SCI_TEXT}" font-family="{FONT_FAMILY}" font-size="14">1. Calcolo del Discriminante (Δ):</text>
    <text x="56" y="615" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Δ = b² - 4ac = (-5)² - 4(1)(6) = 25 - 24 = 1</text>
    
    <text x="40" y="650" fill="{SCI_TEXT}" font-family="{FONT_FAMILY}" font-size="14">2. Formula Risolutiva Quadratica:</text>
    <text x="56" y="675" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">x = (-b ± √Δ) / 2a = (5 ± √1) / 2</text>

    <text x="40" y="710" fill="{SCI_TEXT}" font-family="{FONT_FAMILY}" font-size="14">3. Calcolo dei Valori Finali:</text>
    <text x="56" y="735" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">x₁ = (5 + 1) / 2 = 6/2 = 3</text>
    <text x="56" y="760" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">x₂ = (5 - 1) / 2 = 4/2 = 2</text>
  </g>
</svg>"""

# SCREEN 5: GRAFICI 2D DI FUNZIONI
def screen_05():
    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Grafico Funzione 2D", back_btn=True, menu_btn=False)}

  <!-- Function Input Bar -->
  <g id="FunctionBar">
    <rect x="20" y="115" width="372" height="56" rx="16" ry="16" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}" stroke-width="1.5"/>
    <text x="40" y="150" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="18" font-weight="600">f(x) =</text>
    <text x="96" y="150" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="18" font-weight="500">sin(x) * 1.5</text>
    
    <!-- Plot button -->
    <rect x="326" y="125" width="56" height="36" rx="12" ry="12" fill="{EQUALS_CONTAINER}"/>
    <text x="354" y="148" fill="{EQUALS_TEXT}" font-family="{FONT_FAMILY}" font-size="13" font-weight="600" text-anchor="middle">Disegna</text>
  </g>

  <!-- Cartesio Canvas Box -->
  <g id="Canvas2D">
    <rect x="20" y="190" width="372" height="480" rx="20" ry="20" fill="#181A20" stroke="{SURFACE_CARD_BORDER}" stroke-width="1"/>
    
    <!-- Grid Lines -->
    <path d="M40 250 H372 M40 310 H372 M40 370 H372 M40 490 H372 M40 550 H372 M40 610 H372" stroke="#242832" stroke-width="1"/>
    <path d="M82 210 V650 M144 210 V650 M268 210 V650 M330 210 V650" stroke="#242832" stroke-width="1"/>

    <!-- Main Axis X & Y -->
    <line x1="40" y1="430" x2="372" y2="430" stroke="#4F5360" stroke-width="2"/>
    <line x1="206" y1="210" x2="206" y2="650" stroke="#4F5360" stroke-width="2"/>

    <text x="365" y="422" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="12">X</text>
    <text x="214" y="222" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="12">Y</text>
    <text x="214" y="444" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="11">0</text>

    <!-- Sine Wave Curve: f(x) = sin(x) -->
    <path d="M 50 430 Q 89 320, 128 430 T 206 430 T 284 430 T 362 430" stroke="{GREEN_ACCENT}" stroke-width="3" fill="none"/>

    <!-- Peak and Intercept Points -->
    <circle cx="128" cy="430" r="4" fill="#FFD152"/>
    <circle cx="167" cy="508" r="4" fill="{RED_ACCENT}"/>
    <circle cx="206" cy="430" r="4" fill="#FFD152"/>
    <circle cx="245" cy="352" r="4" fill="{TEXT_ACCENT}"/>

    <!-- On Canvas Zoom Buttons -->
    <g transform="translate(336, 560)">
      <circle cx="20" cy="20" r="18" fill="{NUM_CONTAINER}" stroke="{OP_CONTAINER}"/>
      <text x="20" y="26" fill="{TEXT_PRIMARY}" font-size="20" font-weight="400" text-anchor="middle">+</text>

      <circle cx="20" cy="64" r="18" fill="{NUM_CONTAINER}" stroke="{OP_CONTAINER}"/>
      <text x="20" y="70" fill="{TEXT_PRIMARY}" font-size="20" font-weight="400" text-anchor="middle">−</text>
    </g>
  </g>

  <!-- Quick Analysis Stats -->
  <g id="AnalysisCard">
    <rect x="20" y="690" width="372" height="135" rx="18" ry="18" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}"/>
    <text x="40" y="722" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="600">Analisi Funzione</text>
    
    <text x="40" y="750" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Periodo (T): <tspan fill="{TEXT_PRIMARY}" font-weight="500">2π ≈ 6.283</tspan></text>
    <text x="40" y="775" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Ampiezza Max: <tspan fill="{GREEN_ACCENT}" font-weight="500">y = 1.5</tspan> | Min: <tspan fill="{RED_ACCENT}" font-weight="500">y = -1.5</tspan></text>
    <text x="40" y="800" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Intersezioni Asse X: <tspan fill="{TEXT_PRIMARY}">x = k·π</tspan></text>
  </g>
</svg>"""

# SCREEN 6: CONVERTITORE UNITA
def screen_06():
    c_btn_w = 116
    c_btn_h = 58
    c_rx = 18
    c_start_y = 480

    pad = []
    # Row 1: 7, 8, 9
    pad.append(render_btn(20, c_start_y, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "7", NUM_TEXT, 24))
    pad.append(render_btn(148, c_start_y, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "8", NUM_TEXT, 24))
    pad.append(render_btn(276, c_start_y, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "9", NUM_TEXT, 24))

    # Row 2: 4, 5, 6
    pad.append(render_btn(20, c_start_y + 70, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "4", NUM_TEXT, 24))
    pad.append(render_btn(148, c_start_y + 70, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "5", NUM_TEXT, 24))
    pad.append(render_btn(276, c_start_y + 70, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "6", NUM_TEXT, 24))

    # Row 3: 1, 2, 3
    pad.append(render_btn(20, c_start_y + 140, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "1", NUM_TEXT, 24))
    pad.append(render_btn(148, c_start_y + 140, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "2", NUM_TEXT, 24))
    pad.append(render_btn(276, c_start_y + 140, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "3", NUM_TEXT, 24))

    # Row 4: C, 0, ⌫
    pad.append(render_btn(20, c_start_y + 210, c_btn_w, c_btn_h, c_rx, OP_CONTAINER, "C", RED_ACCENT, 24))
    pad.append(render_btn(148, c_start_y + 210, c_btn_w, c_btn_h, c_rx, NUM_CONTAINER, "0", NUM_TEXT, 24))
    pad.append(render_backspace_btn(276, c_start_y + 210, c_btn_w, c_btn_h, c_rx, OP_CONTAINER, NUM_TEXT))

    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Convertitore Unità", back_btn=True, menu_btn=False)}

  <!-- Category Selection Pills -->
  <g id="Categories">
    <rect x="20" y="115" width="94" height="38" rx="19" ry="19" fill="{EQUALS_CONTAINER}"/>
    <text x="67" y="139" fill="{EQUALS_TEXT}" font-family="{FONT_FAMILY}" font-size="13" font-weight="600" text-anchor="middle">Velocità</text>

    <rect x="122" y="115" width="86" height="38" rx="19" ry="19" fill="{SURFACE_CARD}"/>
    <text x="165" y="139" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13" font-weight="500" text-anchor="middle">Lunghezza</text>

    <rect x="216" y="115" width="76" height="38" rx="19" ry="19" fill="{SURFACE_CARD}"/>
    <text x="254" y="139" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13" font-weight="500" text-anchor="middle">Massa</text>

    <rect x="300" y="115" width="92" height="38" rx="19" ry="19" fill="{SURFACE_CARD}"/>
    <text x="346" y="139" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13" font-weight="500" text-anchor="middle">Volume</text>
  </g>

  <!-- From Input Box -->
  <g id="FromBox">
    <rect x="20" y="180" width="372" height="110" rx="20" ry="20" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}"/>
    <text x="40" y="212" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Da:</text>
    <text x="40" y="240" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600">Chilometri orari (km/h) ▾</text>
    <text x="372" y="265" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="36" font-weight="400" text-anchor="end">100</text>
  </g>

  <!-- Swap Icon Button -->
  <g id="SwapBtn" cursor="pointer">
    <circle cx="206" cy="325" r="22" fill="{SCI_CONTAINER}" stroke="{OP_CONTAINER}"/>
    <path d="M201 321 L211 321 M208 318 L211 321 L208 324" stroke="{TEXT_PRIMARY}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M211 329 L201 329 M204 326 L201 329 L204 332" stroke="{TEXT_PRIMARY}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
  </g>

  <!-- To Output Box -->
  <g id="ToBox">
    <rect x="20" y="340" width="372" height="110" rx="20" ry="20" fill="{SURFACE_CARD}" stroke="{OP_CONTAINER}"/>
    <text x="40" y="372" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">A:</text>
    <text x="40" y="400" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600">Miglia orarie (mph) ▾</text>
    <text x="372" y="425" fill="{GREEN_ACCENT}" font-family="{FONT_FAMILY}" font-size="36" font-weight="500" text-anchor="end">62.1371</text>
  </g>

  <!-- Numerical Pad -->
  <g id="ConverterNumpad">
    {''.join(pad)}
  </g>
</svg>"""

# SCREEN 7: LIVELLA A BOLLA DIGITALE
def screen_07():
    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Livella a Bolla", back_btn=True, menu_btn=False)}

  <!-- Angle Display Header -->
  <g id="DegreesHeader">
    <text x="206" y="170" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="64" font-weight="300" text-anchor="middle">0.0°</text>
    <text x="206" y="200" fill="{GREEN_ACCENT}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600" text-anchor="middle">PERFETTAMENTE A LIVELLO</text>
  </g>

  <!-- Big Concentric Bullseye Bubble Indicator -->
  <g id="BullseyeLevel" transform="translate(206, 420)">
    <circle cx="0" cy="0" r="160" fill="#1A1C22" stroke="{SURFACE_CARD_BORDER}" stroke-width="2"/>
    <circle cx="0" cy="0" r="120" fill="none" stroke="{NUM_CONTAINER}" stroke-width="2" stroke-dasharray="4 4"/>
    <circle cx="0" cy="0" r="80" fill="none" stroke="{OP_CONTAINER}" stroke-width="2"/>
    <circle cx="0" cy="0" r="40" fill="none" stroke="{GREEN_ACCENT}" stroke-width="2" stroke-opacity="0.6"/>

    <line x1="-160" y1="0" x2="160" y2="0" stroke="{OP_CONTAINER}" stroke-width="1.5"/>
    <line x1="0" y1="-160" x2="0" y2="160" stroke="{OP_CONTAINER}" stroke-width="1.5"/>

    <!-- Green Liquid Bubble -->
    <circle cx="2" cy="-1" r="32" fill="{GREEN_ACCENT}" opacity="0.85"/>
    <circle cx="-6" cy="-8" r="8" fill="#FFFFFF" opacity="0.4"/>
    <circle cx="0" cy="0" r="5" fill="#FFFFFF"/>
  </g>

  <!-- Pitch & Roll Cards -->
  <g id="AxesStats">
    <rect x="20" y="650" width="176" height="84" rx="18" ry="18" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}"/>
    <text x="40" y="682" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Inclinazione X (Pitch)</text>
    <text x="40" y="715" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="26" font-weight="500">+0.2°</text>

    <rect x="216" y="650" width="176" height="84" rx="18" ry="18" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}"/>
    <text x="236" y="682" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="13">Inclinazione Y (Roll)</text>
    <text x="236" y="715" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="26" font-weight="500">-0.1°</text>
  </g>

  <!-- Calibration Action -->
  <g id="CalibrateBtn" cursor="pointer">
    <rect x="20" y="760" width="372" height="52" rx="26" ry="26" fill="{NUM_CONTAINER}" stroke="{OP_CONTAINER}"/>
    <text x="206" y="792" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="15" font-weight="600" text-anchor="middle">Calibra Sensore Orizzontale</text>
  </g>
</svg>"""

# SCREEN 8: BUSSOLA PIXEL
def screen_08():
    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Bussola Pixel", back_btn=True, menu_btn=False)}

  <!-- Heading Info -->
  <g id="HeadingHeader">
    <text x="206" y="165" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="54" font-weight="300" text-anchor="middle">42° NE</text>
    <text x="206" y="195" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="15" font-weight="500" text-anchor="middle">Nord-Est • Precisione Elevata</text>
  </g>

  <!-- Compass Rose -->
  <g id="CompassRose" transform="translate(206, 410)">
    <circle cx="0" cy="0" r="160" fill="#181A20" stroke="{SURFACE_CARD_BORDER}" stroke-width="2"/>
    <circle cx="0" cy="0" r="140" fill="none" stroke="#22252C" stroke-width="1.5"/>

    <!-- Degree Ticks -->
    <g stroke="#4F5360" stroke-width="1.5">
      <line x1="0" y1="-155" x2="0" y2="-142"/>
      <line x1="110" y1="-110" x2="100" y2="-100"/>
      <line x1="155" y1="0" x2="142" y2="0"/>
      <line x1="110" y1="110" x2="100" y2="100"/>
      <line x1="0" y1="155" x2="0" y2="142"/>
      <line x1="-110" y1="110" x2="-100" y2="100"/>
      <line x1="-155" y1="0" x2="-142" y2="0"/>
      <line x1="-110" y1="-110" x2="-100" y2="-100"/>
    </g>

    <!-- Cardinal Directions -->
    <text x="0" y="-120" fill="#FF5252" font-family="{FONT_FAMILY}" font-size="18" font-weight="700" text-anchor="middle" dominant-baseline="central">N</text>
    <text x="122" y="0" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600" text-anchor="middle" dominant-baseline="central">E</text>
    <text x="0" y="122" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600" text-anchor="middle" dominant-baseline="central">S</text>
    <text x="-122" y="0" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600" text-anchor="middle" dominant-baseline="central">W</text>

    <!-- Magnetic Needle (orientata a 42 gradi) -->
    <g transform="rotate(-42)">
      <polygon points="0,-115 -14,0 14,0" fill="#FF5252"/>
      <polygon points="0,115 -14,0 14,0" fill="{TEXT_PRIMARY}"/>
      <circle cx="0" cy="0" r="10" fill="{BG_COLOR}" stroke="{TEXT_PRIMARY}" stroke-width="2"/>
    </g>
  </g>

  <!-- GPS Coordinates & Altitude -->
  <g id="GPSCard">
    <rect x="20" y="650" width="372" height="160" rx="20" ry="20" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}"/>
    <text x="44" y="686" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="600">Posizione &amp; Quota</text>
    
    <text x="44" y="720" fill="{TEXT_SECONDARY}" font-size="13">Coordinate:</text>
    <text x="44" y="744" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="16" font-weight="500">41° 54' 38" N • 12° 29' 47" E</text>

    <text x="44" y="780" fill="{TEXT_SECONDARY}" font-size="13">Altitudine: <tspan fill="{GREEN_ACCENT}" font-weight="600">48 m s.l.m.</tspan></text>
  </g>
</svg>"""

# SCREEN 9: RIGHELLO SU SCHERMO
def screen_09():
    cm_ticks = []
    for i in range(1, 28):
        y = 120 + i * 26
        is_cm = (i % 2 == 0)
        cm_num = i // 2
        width = 38 if is_cm else 20
        cm_ticks.append(f'<line x1="0" y1="{y}" x2="{width}" y2="{y}" stroke="{TEXT_PRIMARY}" stroke-width="{2 if is_cm else 1.2}"/>')
        if is_cm:
            cm_ticks.append(f'<text x="{width + 8}" y="{y + 4}" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="12">{cm_num}</text>')

    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="{BG_COLOR}"/>
  {status_bar()}
  {top_bar(title="Righello Digitale", back_btn=True, menu_btn=False)}

  <!-- Ruler Scale -->
  <g id="ScaleCM">
    <rect x="0" y="100" width="70" height="740" fill="#1A1C22"/>
    <line x1="70" y1="100" x2="70" y2="840" stroke="{OP_CONTAINER}" stroke-width="1.5"/>
    {''.join(cm_ticks)}
  </g>

  <!-- Interactive Measurement Marker Slider -->
  <g id="MeasurementMarker">
    <line x1="0" y1="380" x2="{W}" y2="380" stroke="#FFD152" stroke-width="2.5" stroke-dasharray="6 4"/>
    <rect x="180" y="362" width="130" height="36" rx="18" ry="18" fill="#FFD152"/>
    <text x="245" y="386" fill="#3D2A00" font-family="{FONT_FAMILY}" font-size="15" font-weight="700" text-anchor="middle">5.00 cm</text>
  </g>

  <!-- Info Box -->
  <g id="InfoBox">
    <rect x="90" y="720" width="300" height="110" rx="18" ry="18" fill="{SURFACE_CARD}" stroke="{SURFACE_CARD_BORDER}"/>
    <text x="110" y="752" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="14" font-weight="600">Calibrazione Schermo</text>
    <text x="110" y="776" fill="{TEXT_SECONDARY}" font-size="12">Pixel Density: 441 PPI (1:1 scala reale)</text>
    <text x="110" y="800" fill="{TEXT_PRIMARY}" font-size="13">Misura: 5.00 cm / 1.97 in</text>
  </g>
</svg>"""

# SCREEN 10: CRONOLOGIA SHEET
def screen_10():
    def hist_item(y, expr, res, time_ago):
        return f"""
        <g transform="translate(0, {y})">
          <text x="352" y="24" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="18" text-anchor="end">{expr}</text>
          <text x="352" y="60" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="32" font-weight="500" text-anchor="end">{res}</text>
          <text x="20" y="60" fill="#6B6E78" font-family="{FONT_FAMILY}" font-size="12">{time_ago}</text>
          <line x1="20" y1="80" x2="352" y2="80" stroke="{NUM_CONTAINER}" stroke-width="1"/>
        </g>
        """

    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
  <rect width="{W}" height="{H}" fill="#0A0B0E"/>

  <!-- Dimmed Backdrop Preview of main calculator -->
  <g opacity="0.15">
    {screen_01().split('<rect width="412" height="892" fill="#131418"/>')[1].rsplit('</svg>', 1)[0]}
  </g>

  <!-- Overlay Scrim -->
  <rect width="{W}" height="{H}" fill="#000000" opacity="0.45"/>

  <!-- History Bottom/Drop Sheet -->
  <g id="HistorySheet">
    <rect x="20" y="80" width="372" height="640" rx="28" ry="28" fill="#191A20" stroke="#2D3039" stroke-width="1.5"/>
    <rect x="186" y="94" width="40" height="4" rx="2" ry="2" fill="{TEXT_SECONDARY}" opacity="0.5"/>

    <text x="44" y="135" fill="{TEXT_PRIMARY}" font-family="{FONT_FAMILY}" font-size="20" font-weight="600">Cronologia</text>
    
    <g id="ClearBtn" cursor="pointer">
      <rect x="300" y="116" width="68" height="28" rx="14" ry="14" fill="#2E2426"/>
      <text x="334" y="135" fill="{RED_ACCENT}" font-family="{FONT_FAMILY}" font-size="12" font-weight="500" text-anchor="middle">Cancella</text>
    </g>

    <g id="ItemsList" transform="translate(20, 160)">
      {hist_item(0, "450 × 1.22", "= 549", "10 min fa")}
      {hist_item(90, "√144 + 5²", "= 37", "Oggi 14:15")}
      {hist_item(180, "2^10", "= 1024", "Oggi 11:30")}
      {hist_item(270, "sin(90°) + cos(0°)", "= 2", "Ieri")}
      {hist_item(360, "15% × 89.90", "= 13.485", "20 Set")}
      {hist_item(450, "100 km/h in mph", "= 62.137", "18 Set")}
    </g>
  </g>
  {status_bar()}
</svg>"""

# LIST OF ALL SCREENS
screens = [
    ("01_calcolatrice_principale.svg", screen_01),
    ("02_calcolatrice_scientifica.svg", screen_02),
    ("03_menu_strumenti_toolbox.svg", screen_03),
    ("04_risolutore_equazioni.svg", screen_04),
    ("05_grafici_2d.svg", screen_05),
    ("06_convertitore_unita.svg", screen_06),
    ("07_livella_a_bolla.svg", screen_07),
    ("08_bussola_pixel.svg", screen_08),
    ("09_righello_digitale.svg", screen_09),
    ("10_cronologia_sheet.svg", screen_10),
]

for filename, generator in screens:
    path = os.path.join(OUTPUT_DIR, filename)
    with open(path, "w", encoding="utf-8") as f:
        f.write(generator())
    print(f"Generated {filename}")

# ARTBOARD ALL SCREENS COMBINED
COLUMNS = 5
ROWS = 2
GAP_X = 60
GAP_Y = 80
TOTAL_W = COLUMNS * W + (COLUMNS + 1) * GAP_X
TOTAL_H = ROWS * H + (ROWS + 1) * GAP_Y + 120

artboard = [
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {TOTAL_W} {TOTAL_H}" width="{TOTAL_W}" height="{TOTAL_H}">',
    f'<rect width="{TOTAL_W}" height="{TOTAL_H}" fill="#0A0B0E"/>',
    f'<text x="{GAP_X}" y="65" fill="#FFFFFF" font-family="{FONT_FAMILY}" font-size="32" font-weight="700">Pixel Calculator - Design System &amp; Layout Artboards</text>',
    f'<text x="{GAP_X}" y="98" fill="{TEXT_SECONDARY}" font-family="{FONT_FAMILY}" font-size="16">Figma Import Ready • 10 Artboards • 100% Vector Pill Buttons (rx/ry XML) &amp; Material 3 Colors</text>'
]

for idx, (filename, generator) in enumerate(screens):
    col = idx % COLUMNS
    row = idx // COLUMNS
    x = GAP_X + col * (W + GAP_X)
    y = 140 + row * (H + GAP_Y)
    title = filename.replace(".svg", "").replace("_", " ").title()
    artboard.append(f'<g id="Artboard_{idx+1}_{filename[:2]}" transform="translate({x}, {y})">')
    artboard.append(f'<text x="0" y="-16" fill="{TEXT_ACCENT}" font-family="{FONT_FAMILY}" font-size="16" font-weight="600">{title}</text>')
    
    # Strip opening svg and closing svg tag to cleanly embed
    raw = generator()
    # Find first '>' after '<svg'
    svg_start = raw.find('>', raw.find('<svg')) + 1
    svg_end = raw.rfind('</svg>')
    inner = raw[svg_start:svg_end]
    artboard.append(inner)
    artboard.append('</g>')

artboard.append('</svg>')

artboard_path = os.path.join(OUTPUT_DIR, "tutte_le_schermate_artboard.svg")
with open(artboard_path, "w", encoding="utf-8") as f:
    f.write("\n".join(artboard))
print(f"Generated tutte_le_schermate_artboard.svg ({TOTAL_W}x{TOTAL_H})")

print("All SVG assets successfully upgraded for Figma!")
