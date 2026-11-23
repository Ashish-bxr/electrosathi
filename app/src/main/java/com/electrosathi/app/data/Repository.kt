package com.electrosathi.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

/**
 * Single source of truth for app content.
 *
 * HOW IT WORKS (Batch 14 — fully offline):
 * - On every app start, sync() loads the admin-published lists from
 *   the on-device SQLite store (AppDb / LocalContent) into the caches.
 * - A collection with no published list shows the built-in content
 *   below; a published list replaces it wholesale (full-list rule).
 * - The admin panel writes to the same local store; a successful
 *   write reloads the caches, so new content appears immediately —
 *   no internet, no server, no app update.
 */
object Repository {

    // ------------------------------------------------------------------
    // Local fallback content (also the seed your admin panel can extend).
    // ------------------------------------------------------------------
    private val localExams = listOf(
        Exam(
            id = "gate", name = "GATE (EC)", category = "Higher Studies",
            overview = "The Graduate Aptitude Test in Engineering (Electronics & Communication paper) is the gateway to M.Tech programmes at IITs/NITs and to PSU jobs. Conducted jointly by IISc and the IITs, once a year.",
            eligibility = "B.E./B.Tech in ECE/EEE or related branches (final-year students eligible). No age limit.",
            pattern = "100 marks • 65 questions • 3 hours. General Aptitude (15) + Engineering Mathematics (13) + Subject (72). Negative marking on MCQs.",
            syllabus = "Networks, Signals & Systems, Electronic Devices, Analog & Digital Circuits, Control Systems, Communications, Electromagnetics, Engineering Mathematics.",
            dates = "GATE 2027 exam days: 6 Feb 2027, 7 Feb 2027, 13 Feb 2027, 14 Feb 2027, 20 Feb 2027 and 21 Feb 2027 — conducted by IIT Madras. Results on 19 Mar 2027. (The EC paper's exact day/session is announced with the admit card.)",
            papers = "Previous 15 years' papers with solutions (arriving in-app in Phase 3).",
            cutoff = "Qualifying ~25–30/100 (General). PSU shortlists typically need 60+."
        ),
        Exam(
            id = "ese", name = "ESE / IES", category = "Government Jobs",
            overview = "The UPSC Engineering Services Examination recruits engineers for railways, telecom, defence and central engineering services — the most prestigious government track for ECE graduates.",
            eligibility = "B.E./B.Tech in a relevant branch, age 21–30, Indian citizen.",
            pattern = "Prelims (500 marks) + Mains (600 marks) + Personality Test (200 marks).",
            syllabus = "Full ECE core + General Studies & Engineering Aptitude paper.",
            dates = "ESE 2027 Prelims: 31 Jan 2027 — notified by UPSC on 16 Sep 2026; applications closed 6 Oct 2026. Mains on 18 Jun 2027. Dates as per the official UPSC examination calendar.",
            papers = "Last 10 years' papers (arriving in-app in Phase 3).",
            cutoff = "Final selection typically needs ~55–60% overall (varies by year)."
        ),
        Exam(
            id = "isro", name = "ISRO Scientist/Engineer 'SC'", category = "Government Jobs",
            overview = "ISRO's own recruitment for Scientist/Engineer-SC in Electronics — work on satellites, launch vehicles and ground systems.",
            eligibility = "B.E./B.Tech with 65% or CGPA 6.84/10. Age up to 28 (relaxations as per rules).",
            pattern = "Written test (80 questions, 90 minutes) + interview for shortlisted candidates.",
            syllabus = "Core ECE: networks, electronic devices, digital electronics, communications, EMFT.",
            dates = "2026 cycle: CBT on 19 Oct 2026 — 175 posts, including 100 in Electronics.",
            papers = "Previous papers (arriving in-app in Phase 3).",
            cutoff = "Written shortlist around ~150/240, varies by cycle."
        ),
        Exam(
            id = "sscje", name = "SSC JE", category = "Government Jobs",
            overview = "Staff Selection Commission Junior Engineer — recruits for CPWD, MES, BRO and other central departments.",
            eligibility = "Diploma or B.E./B.Tech in the relevant branch. Age up to 30–32 depending on post.",
            pattern = "Paper 1 (CBT, 200 marks) + Paper 2 (descriptive, 300 marks).",
            syllabus = "Basic electrical/electronics engineering + general awareness and reasoning.",
            dates = "Notification usually mid-year.",
            papers = "Last 5 years' papers (arriving in-app in Phase 3).",
            cutoff = "Paper 1 UR cutoff roughly ~120/200 (varies)."
        )
    )

    private val localProjects = listOf(
        Project(
            id = "led555", title = "LED Blinker with 555 Timer",
            level = "Basic", domain = "Analog", time = "2 hrs", cost = "~₹150",
            overview = "The classic first build: a 555 timer wired as an astable multivibrator, blinking two LEDs alternately. If you can build this, you can read a datasheet.",
            components = listOf(
                "NE555 timer IC", "2× LEDs", "2× 470Ω resistors",
                "10kΩ + 100kΩ resistors", "10µF capacitor",
                "9V battery + clip", "Breadboard + jumper wires"
            ),
            steps = listOf(
                "Place the 555 IC across the breadboard's centre gap.",
                "Wire the astable circuit exactly as per the diagram.",
                "Connect each LED through its 470Ω current-limiting resistor.",
                "Connect the 9V battery and watch the LEDs blink.",
                "Swap resistor values to change the blink rate — observe and note why."
            ),
            learns = "RC timing, astable multivibrator operation, reading datasheets, LED current limiting."
        ),
        Project(
            id = "espweather", title = "ESP32 Weather Station",
            level = "Intermediate", domain = "IoT", time = "1 week", cost = "~₹1,200",
            overview = "Build a Wi-Fi weather station that reads temperature/humidity and shows it on an OLED display — then pushes the data to the cloud.",
            components = listOf(
                "ESP32 dev board", "DHT22 temperature/humidity sensor",
                "0.96\" OLED display (I2C)", "Jumper wires", "Micro-USB cable"
            ),
            steps = listOf(
                "Wire the DHT22 to a GPIO pin and the OLED over I2C.",
                "Install the ESP32 board package, DHT and OLED libraries in Arduino IDE.",
                "Flash the firmware and verify readings on the serial monitor.",
                "Render temperature/humidity on the OLED.",
                "Push readings to ThingSpeak over Wi-Fi and view the graphs online."
            ),
            learns = "I2C/SPI interfacing, sensor libraries, Wi-Fi + HTTP, cloud dashboards."
        ),
        Project(
            id = "linefollower", title = "Line Follower Robot",
            level = "Intermediate", domain = "Robotics", time = "2 weeks", cost = "~₹2,500",
            overview = "A two-wheeled robot that follows a black line using IR sensors — the gateway drug of robotics, and a lesson in control theory.",
            components = listOf(
                "Arduino Nano", "IR sensor array (5-channel)", "L293D motor driver",
                "2× BO motors + wheels", "Robot chassis", "18650 batteries + holder"
            ),
            steps = listOf(
                "Assemble the chassis, motors and wheels.",
                "Mount the IR array low, close to the ground.",
                "Wire sensors → Arduino → motor driver → motors.",
                "Calibrate sensor thresholds on your track lighting.",
                "Tune the PID constants until it corners without wobbling."
            ),
            learns = "PID control, motor drivers, sensor calibration, power budgeting."
        ),
        Project(
            id = "lm386-amp", title = "LM386 Audio Amplifier",
            level = "Intermediate", domain = "Analog", time = "2 days", cost = "~₹450",
            overview = "The definitive analog build: a complete low-voltage audio power amplifier around the LM386 IC that takes your phone's headphone output and drives a speaker loud enough to fill a room. One IC, a handful of passives, gain selectable between 20 and 200 — this is the circuit that teaches decoupling, coupling and stability all at once.",
            components = listOf(
                "LM386 audio amplifier IC",
                "8Ω 0.5W speaker",
                "10kΩ potentiometer (volume control)",
                "220µF electrolytic capacitor (output coupling)",
                "10µF electrolytic capacitor (sets gain to 200)",
                "47nF ceramic capacitor + 10Ω resistor (Zobel network)",
                "100µF electrolytic capacitor (supply decoupling)",
                "3.5mm audio jack / aux cable",
                "9V battery + clip (or a 5V USB supply)",
                "Breadboard first, then perfboard + soldering kit"
            ),
            steps = listOf(
                "Build on the breadboard first: seat the LM386, wire ground (pin 4) and supply (pin 6), and put the 100µF decoupling capacitor straight across the supply rails, close to the IC.",
                "Wire the input: phone/aux signal through the 10kΩ potentiometer (volume control) into pin 3, with pin 2 tied to ground.",
                "Connect the 10µF capacitor between pins 1 and 8 — this sets the gain to 200. Leave it out and the gain falls to 20; try both and hear the difference.",
                "Wire the output (pin 5) through the 220µF capacitor to the speaker, and add the 47nF + 10Ω Zobel network from pin 5 to ground — it keeps the amplifier stable at high frequencies.",
                "Power up with the volume low, play music from your phone and raise the volume slowly. You should get clear, room-filling sound from the small speaker.",
                "Test and tune: early distortion means too much gain or weak decoupling — re-check the 100µF cap and try gain 20. Whistling or oscillation means long leads — shorten them and confirm the Zobel network. Then solder the final circuit onto perfboard and box it."
            ),
            learns = "Audio power amplification, IC gain setting with external components, output coupling, Zobel stability networks, supply decoupling, and the breadboard-to-perfboard finishing process."
        ),
        // Batch 8 — the digital counterpart of the LM386 build: a real
        // 0–99 counter (2× CD4026 + 555 clock, cascaded via carry-out).
        Project(
            id = "counter4026", title = "0–99 Digital Counter with 7-Segment Displays",
            level = "Basic", domain = "Digital", time = "1 day", cost = "~₹350",
            overview = "Count anything — laps, visitors, parts coming off a line — from 0 to 99 on two glowing 7-segment displays. Two CD4026 counter ICs do all the work: each one counts 0–9 and drives its own display directly, with no separate decoder chip. A 555 timer provides a steady one-pulse-per-second clock, and a debounced push button lets you count events by hand. No code anywhere — just datasheet reading, cascading and clean wiring.",
            components = listOf(
                "2× CD4026 decade counter / 7-segment driver ICs",
                "2× 7-segment LED displays (common cathode)",
                "NE555 timer IC (the clock)",
                "14× 330Ω resistors (one in series with each segment)",
                "10kΩ + 68kΩ resistors and a 10µF electrolytic capacitor (555 timing parts — about 1 pulse per second)",
                "100nF ceramic capacitors (switch debounce + supply decoupling)",
                "2× tactile push buttons (manual count + reset)",
                "10kΩ resistors (pull-downs for the clock and reset lines)",
                "9V battery + clip (or a 5V USB supply)",
                "Breadboard first, then perfboard + soldering kit"
            ),
            steps = listOf(
                "Build the clock first: wire the 555 as an astable with the 10kΩ and 68kΩ timing resistors and the 10µF capacitor, giving roughly one pulse per second. Touch an LED with a 470Ω resistor to pin 3 and watch it blink before you trust the clock with the counters.",
                "Seat the units CD4026: pin 16 to the supply, pin 8 to ground, the clock-enable (pin 2) tied to ground so the chip always counts, and the display-enable (pin 3) tied to the supply so the display stays on. Add a 100nF decoupling capacitor across its supply pins.",
                "Wire its seven segment outputs (pins 6, 7, 9, 10, 11, 12, 13) through 330Ω resistors to the matching a–g pins of one display, and feed pin 1 (clock) from the 555. Power up: the display should count 0, 1, 2 … 9, 0. A wrong digit means two segment wires are swapped — check against the CD4026 datasheet pinout instead of guessing.",
                "Cascade the tens digit: wire the second CD4026 and display exactly the same way, but feed its clock (pin 1) from the first chip's carry-out (pin 5). The carry-out completes one pulse per full 0–9 cycle, so the tens digit steps exactly when the units digit rolls from 9 back to 0 — the counter now runs 00 to 99.",
                "Add hand control: a push button with a 100nF capacitor across it and a 10kΩ pull-down (so one press can't bounce into five counts) that can be switched onto the clock line to count events manually, plus a second button that pulls the reset pins (pin 15 on both chips) high to snap the display back to 00.",
                "Test the full roll-over: let it run past 99 and confirm it wraps cleanly to 00. If the tens digit jumps early, the carry wire is picking up noise from the clock line — shorten it and re-check the decoupling capacitors. Then solder the circuit onto perfboard so the counter survives being handled."
            ),
            learns = "Decade counters with built-in 7-segment decoding, cascading counters via the carry-out pin, 555 astable clock design, switch debouncing, LED current limiting, and datasheet-driven wiring."
        ),
        Project(
            id = "fpgasiggen", title = "FPGA-based Signal Generator",
            level = "Advanced", domain = "VLSI / FPGA", time = "1 month", cost = "~₹8,000",
            overview = "Implement a Direct Digital Synthesis (DDS) signal generator in Verilog on an FPGA, output through a DAC, verified on an oscilloscope.",
            components = listOf(
                "iCE40 or equivalent FPGA dev board", "DAC module (e.g. MCP4921)",
                "Access to an oscilloscope", "Jumper wires"
            ),
            steps = listOf(
                "Write the Verilog DDS: phase accumulator + sine lookup table.",
                "Simulate with a testbench and verify the waveform.",
                "Synthesise, apply pin constraints, generate the bitstream.",
                "Flash the FPGA and probe the DAC output on the scope.",
                "Add frequency/AM control via switches or UART."
            ),
            learns = "Verilog, DDS theory, simulation testbenches, timing constraints, DAC interfacing."
        )
    )

    private val localInternships = listOf(
        Internship(
            id = "drdo-rci", title = "Graduate / Technician / Trade Apprentices — 195 seats",
            org = "Research Centre Imarat (DRDO), Hyderabad",
            type = "Government", paid = "Paid (as per govt norms)", location = "Hyderabad",
            eligibility = "70% or CGPA 7.5+, pass-out years 2022–2026, age 18+, no prior apprenticeship.",
            deadline = "1 Nov 2026",
            description = "One-year apprenticeship at DRDO's missile-technology lab: 50 Graduate Apprentice seats (B.E./B.Tech incl. ECE/EEE/Instrumentation), 30 Technician Apprentice (Diploma incl. ECE), 115 Trade Apprentice (ITI: Electronic Mechanic, Electrician, COPA). Apply via NATS 2.0 (graduate/diploma) or the Apprenticeship India portal (ITI). Selection on academic merit + interview."
        ),
        Internship(
            id = "qualcomm-hw", title = "Interim Engineering Intern — Hardware",
            org = "Qualcomm", type = "Private", paid = "Paid",
            location = "Bangalore / Hyderabad",
            eligibility = "Pre-final year B.E./B.Tech in ECE/EEE or related.",
            deadline = "Rolling",
            description = "Hardware engineering intern role on real chip-development teams. Strong digital design and scripting fundamentals expected.",
            // Batch 8 — Apply opens Qualcomm's official careers site
            // (qualcomm.com/company/careers, verified 10 Nov 2026).
            link = "https://www.qualcomm.com/company/careers"
        ),
        Internship(
            id = "ti-embed", title = "Embedded Software Intern",
            org = "Texas Instruments", type = "Private", paid = "Paid",
            location = "Bangalore",
            eligibility = "B.E./B.Tech ECE/EEE with strong C and microcontroller fundamentals.",
            deadline = "Rolling",
            description = "Work on embedded software for TI's microcontroller and analog product lines, alongside full-time engineers.",
            // Batch 8 — Apply opens TI's official careers site
            // (ti.com/careers, verified 10 Nov 2026).
            link = "https://www.ti.com/careers"
        ),
        // Batch 7 — verified on the official DoS/ISRO scheme page
        // (isro.gov.in/InternshipAndProjects.html, checked 10 Nov 2026):
        // standing scheme, unpaid by rule, per-centre intakes.
        Internship(
            id = "isro-internship", title = "ISRO Internship & Student Project Trainee Scheme",
            org = "Indian Space Research Organisation (ISRO)",
            type = "Government", paid = "Unpaid",
            location = "ISRO centres across India",
            eligibility = "Indian citizens pursuing UG/PG/PhD in Science/Technology with at least 60% aggregate or CGPA 6.32/10. Project trainees (B.E./B.Tech) must have completed the 6th semester.",
            deadline = "Rolling — apply through your chosen ISRO centre",
            description = "The Department of Space / ISRO's standing scheme for academic internships (up to 45 days) and curriculum project work at ISRO centres — URSC Bengaluru, VSSC Thiruvananthapuram, SAC Ahmedabad, SDSC Sriharikota, ISTRAC Bengaluru and more, including satellite electronics, telemetry and ground systems. ISRO pays no stipend under this scheme; a certificate is awarded on satisfactory completion. Each centre runs its own intake — the official scheme page links every centre's student page.",
            link = "https://www.isro.gov.in/InternshipAndProjects.html"
        ),
        // Batch 7 — verified on CERN's official careers site
        // (careers.cern/summer, checked 10 Nov 2026): the live
        // SUMM-2027 posting, incl. an Electronics Engineering category.
        // CERN is intergovernmental — the type field only offers
        // Government/Private, so the description states what CERN is.
        Internship(
            id = "cern-summer-2027", title = "CERN Summer Student Programme 2027",
            org = "CERN — European Organization for Nuclear Research",
            type = "Private", paid = "Paid",
            location = "Geneva, Switzerland",
            eligibility = "Bachelor's/Master's students (not PhD) in Physics, Engineering, Computer Science or Mathematics, with at least 6 semesters of full-time study completed by summer 2027; must remain enrolled full-time during the programme; good knowledge of English.",
            deadline = "End of January 2027",
            description = "Spend 8–13 weeks at CERN in Geneva working on a real scientific or technical project — the 2027 call lists projects across many fields, including Electronics Engineering — alongside the famous Summer Student Lecture Programme. CERN is an intergovernmental research organisation; it pays a subsistence allowance of 94 CHF per calendar day, covers health insurance, and helps with accommodation and (depending on circumstances) travel. Open to students of all nationalities.",
            link = "https://careers.cern/summer"
        )
    )

    // Live hackathons — the same cleaned list the bulk-import seed
    // publishes (7 Nov 2026 cleanup: only live entries are kept here,
    // because Firestore content replaces this list wholesale on sync).
    private val localHackathons = listOf(
        Hackathon(
            id = "code-for-billion-2026", name = "Code for a Billion — Bharat Agentic-AI Hackathon",
            org = "Code for India", deadline = "15 Nov 2026 (project deployment)",
            mode = "Online", location = "Online",
            prize = "₹18 lakh pool + incubation fast-track",
            description = "Build agentic-AI open-source public goods across 10 impact areas. Submit a deployed cloud project + public GitHub repo + 2–3 min demo video. Winners announced 5 Dec 2026. NASSCOM + MeitY FutureSkills Prime as knowledge partners.",
            link = "https://codeforindia.org/hackathon"
        ),
        Hackathon(
            id = "homingo-2026", name = "Homingo Free Online AI Hackathon",
            org = "Homingo (community)", deadline = "15 Nov 2026",
            mode = "Online", location = "Online",
            prize = "₹5,000 winner + recommendation certificates (top 3) + participation certificate for all submitters",
            description = "National-scale online AI-engineering hackathon, open to all of India. Tracks: Agents, Automation, LLM apps — any domain, real problems. Solo or teams of 2–4. Registration 1 Oct – 15 Nov; kick-off 21 Nov. Certificates for everyone who submits; standout builders get a path to internships/freelance work with SurgeLabs.",
            link = "https://hackathon.homingo.co.in/"
        )
    )

    // E-Library shelf — exactly the books the Library tab has always
    // shown (format alternates PDF / Read, ratings as displayed).
    private val localBooks = listOf(
        Book(
            id = "electronic-devices", title = "Electronic Devices",
            author = "Boylestad • Millman & Halkias", format = "PDF", rating = "4.8"
        ),
        Book(
            id = "microelectronics", title = "Microelectronics",
            author = "Sedra/Smith • Razavi", format = "Read", rating = "4.7"
        ),
        Book(
            id = "digital-design", title = "Digital Design",
            author = "Morris Mano • Wakerly", format = "PDF", rating = "4.9",
            cover = "book_digital"
        ),
        Book(
            id = "signals-and-systems-book", title = "Signals & Systems",
            author = "Oppenheim • Lathi", format = "Read", rating = "4.6",
            cover = "book_signals"
        ),
        Book(
            id = "communications", title = "Communications",
            author = "Haykin • Proakis", format = "PDF", rating = "4.8"
        ),
        Book(
            id = "electromagnetics", title = "Electromagnetics",
            author = "Sadiku • Hayt", format = "Read", rating = "4.7"
        ),
        // Batch 7 — the standard Analog Circuits text, with bundled
        // cover art (res/drawable-nodpi/book_analog.png). The cover
        // field is set only here in the built-in list; books published
        // from the Admin Panel keep code-drawn covers.
        Book(
            id = "microelectronic-circuits", title = "Microelectronic Circuits",
            author = "Adel S. Sedra • Kenneth C. Smith", format = "PDF", rating = "4.9",
            cover = "book_analog"
        )
    )

    // Study Hub subject cards — exactly the values the hub has always
    // shown. Thumbnails/accents stay in the UI, keyed by list position.
    private val localSubjects = listOf(
        StudySubject(
            id = "analog-circuits", name = "Analog Circuits",
            progressPercent = 65, topicsDone = 12, topicsTotal = 18,
            nextTopic = "BJT Biasing"
        ),
        StudySubject(
            id = "digital-electronics", name = "Digital Electronics",
            progressPercent = 40, topicsDone = 8, topicsTotal = 20,
            nextTopic = "Karnaugh Maps (K-Maps)"
        ),
        StudySubject(
            id = "signals-and-systems", name = "Signals & Systems",
            progressPercent = 15, topicsDone = 3, topicsTotal = 18,
            nextTopic = "Fourier Series"
        )
    )

    // ------------------------------------------------------------------
    // Subject contents + flashcard decks (Batches 7–8). Built-in only
    // for now — these are long-form study material, not admin-published
    // content, so they are NOT synced from Firestore in these batches.
    // Batch 7 added Analog Circuits; Batch 8 completed the set with
    // Digital Electronics and Signals & Systems, so every Study Hub
    // subject now has full contents and a flashcard deck. Subjects with
    // no entry here show an honest "being prepared" state on their
    // contents page; never invent topics for them.
    // ------------------------------------------------------------------
    private val localSubjectTopics: Map<String, List<SubjectTopic>> = mapOf(
        "analog-circuits" to listOf(
            SubjectTopic(
                "Semiconductor Diodes & Rectifiers",
                "A pn junction conducts only when forward biased (anode positive), with a knee voltage of about 0.7 V for silicon and 0.3 V for germanium. Reverse bias widens the depletion region until breakdown — Zener or avalanche. In rectifiers, a half-wave circuit uses only one half-cycle (ripple factor 1.21), while a full-wave bridge uses both (ripple factor 0.48). A capacitor filter smooths the output further by charging to the peak and discharging slowly into the load."
            ),
            SubjectTopic(
                "BJT Biasing & the Q-Point",
                "Biasing sets the transistor's DC operating point (ICQ, VCEQ) near the middle of the load line, so the signal can swing both ways without clipping. The voltage-divider (self-bias) circuit is the most stable arrangement: the divider holds the base voltage nearly fixed, while the emitter resistor fights thermal runaway through negative feedback. A good design makes the divider current about ten times the base current, so β variations barely move the Q-point. A Q-point drifting toward saturation or cutoff shows up directly as distortion."
            ),
            SubjectTopic(
                "BJT Amplifiers",
                "In a common-emitter (CE) amplifier the input drives the base and the output is taken at the collector; the voltage gain is approximately −RC/re′, where re′ ≈ 25 mV/IE is the emitter's intrinsic AC resistance. The CE stage gives high gain with a 180° phase shift. The common-collector (emitter follower) has gain near unity but high input and low output impedance — ideal as a buffer. The common-base stage suits high frequencies. Coupling and bypass capacitors behave as shorts at signal frequencies; bypassing the emitter resistor removes AC degeneration and raises the gain."
            ),
            SubjectTopic(
                "MOSFET Basics & Biasing",
                "A MOSFET controls its drain current with gate voltage, and the insulated gate draws almost no current, giving very high input impedance. An n-channel enhancement MOSFET conducts only when VGS exceeds the threshold voltage Vth; in saturation, ID = k(VGS − Vth)². Biasing is usually done with a voltage divider plus a source resistor, or a constant-current source where precision matters. At typical operating currents the drain current falls as temperature rises, so MOSFETs resist the thermal runaway that BJTs suffer from."
            ),
            SubjectTopic(
                "Op-Amp Fundamentals",
                "An operational amplifier is a very high-gain differential amplifier. The ideal op-amp has infinite open-loop gain, infinite input impedance, zero output impedance, infinite bandwidth and zero offset voltage. When negative feedback is applied, two golden rules follow: (1) no current flows into either input terminal, and (2) the feedback holds the two inputs at the same voltage — the 'virtual short', which becomes a 'virtual earth' when the non-inverting input is grounded. Nearly every op-amp circuit is analysed with just these two rules."
            ),
            SubjectTopic(
                "Op-Amp Applications",
                "The inverting amplifier's gain is set purely by a resistor ratio: −Rf/R1. The non-inverting amplifier gives 1 + Rf/R1 with very high input impedance. Add several input resistors to the same inverting node and the circuit becomes a summer, adding weighted inputs together. Interchanging the roles of R and C produces a differentiator (output proportional to the input's rate of change) or an integrator (output proportional to accumulated input — the heart of ramp generators and analogue computation). Remove the feedback entirely and the op-amp becomes a comparator, its output slamming between the supply rails."
            ),
            SubjectTopic(
                "Feedback & Oscillators",
                "Feedback returns a fraction β of the output to the input. Negative feedback trades excess gain for quality: bandwidth widens by the factor (1 + Aβ), while distortion and sensitivity to device variations fall by the same factor. Make the feedback positive and the circuit can oscillate on its own: the Barkhausen criterion requires loop gain Aβ = 1 at exactly 0° (360°) total phase shift. RC phase-shift and Wien-bridge oscillators set frequency with resistors and capacitors; LC oscillators (Hartley, Colpitts) use a resonant tank circuit for higher frequencies."
            ),
            SubjectTopic(
                "Active Filters",
                "Active filters combine op-amps with RC networks to shape a circuit's frequency response — without inductors. A first-order section rolls off at 20 dB/decade, and every additional order adds another 20 dB/decade. Butterworth filters give the flattest passband, Chebyshev filters a steeper skirt at the cost of passband ripple, and Bessel filters the most linear phase response. The Sallen-Key topology builds a second-order low-pass or high-pass section from one op-amp, two resistors and two capacitors, and cascading sections makes higher-order filters practical."
            )
        ),
        "digital-electronics" to listOf(
            SubjectTopic(
                "Number Systems & Codes",
                "Digital systems count in binary (base 2), with octal (base 8) and hexadecimal (base 16) as compact shorthand — each hex digit is exactly four bits. Convert by grouping bits or by repeated division, and handle fractions by repeated multiplication. Negative numbers are stored in two's complement form, which turns subtraction into addition: an n-bit two's complement number spans −2^(n−1) to +2^(n−1) − 1. Beyond numbers, codes carry information: BCD gives every decimal digit its own four bits, Gray code changes only one bit at a time between neighbours, excess-3 is a self-complementing BCD variant, and ASCII encodes characters."
            ),
            SubjectTopic(
                "Boolean Algebra & Logic Gates",
                "Boolean algebra works with just two values, 0 and 1, and three basic operations — AND, OR and NOT. Its theorems do the heavy lifting in circuit simplification: the commutative, associative and distributive laws, absorption (A + AB = A), and De Morgan's laws, which let you swap AND and OR by complementing everything. The physical gates are AND, OR, NOT, NAND, NOR, XOR and XNOR. NAND and NOR are called universal gates because either one alone can build every other gate. Logic functions are written as sums of products (SOP) or products of sums (POS), read straight off a truth table as minterms or maxterms."
            ),
            SubjectTopic(
                "Karnaugh Maps & Combinational Design",
                "A Karnaugh map lays the truth table out in Gray-code order so that logically adjacent cells differ in exactly one variable. Circling groups of 1s — in sizes that are powers of two (1, 2, 4, 8…), wrapping around the edges — deletes the variables that change inside the group and leaves a minimal sum-of-products expression. Don't-care conditions, marked X, can be treated as 1 or 0, whichever makes the groups bigger. The design flow is always the same: specification → truth table → K-map minimisation → gate circuit. Watch out for static hazards — brief glitches when two paths change at slightly different times — cured by adding the redundant consensus term."
            ),
            SubjectTopic(
                "Multiplexers, Decoders & Encoders",
                "A multiplexer is a data selector: with n select lines it routes one of 2^n inputs to a single output, like an electronic rotary switch. Because its output is a sum of selected minterms, one MUX can also implement any logic function of its select variables. A decoder does the reverse journey, activating exactly one of 2^n outputs for each input code — a 3-to-8 decoder generates all eight minterms, and decoder-plus-OR-gates is a standard way to build combinational functions and memory address decoding. Encoders compress many input lines into a code, and a priority encoder resolves the case where several inputs are active at once. The BCD-to-7-segment decoder/driver is the pair you meet in every display circuit."
            ),
            SubjectTopic(
                "Flip-Flops & Latches",
                "A latch is level-sensitive: its output follows the input while the enable is active. A flip-flop is edge-triggered: it samples the input only at the active clock edge and holds it otherwise — that one difference organises all of synchronous design. The D flip-flop simply captures the input; the T flip-flop toggles when T = 1 and divides frequency by two; the JK flip-flop removes the SR flip-flop's forbidden state by making J = K = 1 mean toggle. In a level-clocked JK, that toggle can repeat uncontrollably during one long clock pulse — the race-around condition — which master–slave construction and edge triggering eliminate. Two numbers govern reliable capture: setup time, how long data must be stable before the edge, and hold time, how long it must stay stable after it."
            ),
            SubjectTopic(
                "Counters & Shift Registers",
                "Counters are flip-flop chains that step through a fixed sequence of states; the number of states is the modulus, and a mod-n counter needs at least ⌈log₂ n⌉ flip-flops. In a ripple (asynchronous) counter each stage is clocked by the previous one — simple, but the propagation delays add up and the outputs briefly pass through false states. A synchronous counter clocks every flip-flop together and computes the next state with combinational logic, so it runs far faster. A decade counter is mod-10, counting in BCD. Ring and Johnson (twisted-ring) counters trade flip-flops for simple decoding. Shift registers move data serially — SISO, SIPO, PISO and PIPO — and appear everywhere: serial links, data delay, pseudo-random sequences, and timing generation."
            ),
            SubjectTopic(
                "ADC & DAC",
                "Converters bridge the analogue and digital worlds. A DAC turns a digital word into a proportional voltage or current: the binary-weighted resistor DAC is simple but needs awkward resistor ratios, while the R-2R ladder uses only two resistor values and scales well. An ADC does the reverse: the flash ADC compares the input against 2^n − 1 references at once and is the fastest, the successive-approximation (SAR) ADC homes in bit by bit and balances speed against cost, and the dual-slope ADC integrates over a fixed time and trades speed for superb noise rejection. The key specification is resolution: with n bits and full-scale voltage VFS, one LSB is VFS / 2^n, and the quantisation error is at most ±½ LSB. Sampling must also obey the Nyquist rate — at least twice the highest input frequency."
            ),
            SubjectTopic(
                "Memory & Programmable Logic",
                "Memory stores bits in addressable cells: RAM loses its contents when power goes (volatile) and reads/writes freely, while ROM is non-volatile and programmed once or rarely. Inside, a decoder selects the addressed row — which is why a ROM is really a fixed combinational circuit that can implement any truth table of its size. Programmable logic generalises that idea: a PLA has both the AND and OR planes programmable, a PAL fixes the OR plane for speed and simplicity, and PROMs serve as one-time code and lookup storage. CPLDs and FPGAs scale the same principle up to whole systems. The specifications that matter are capacity (words × bits), access time, and power."
            )
        ),
        "signals-and-systems" to listOf(
            SubjectTopic(
                "Signals & Their Classification",
                "A signal is any quantity that carries information as it varies with time. Signals are classified along several axes: continuous-time versus discrete-time, analogue versus digital, deterministic (described by a formula) versus random, and periodic — x(t + T) = x(t) for a fundamental period T — versus aperiodic. Energy and power split signals into two families: an energy signal has finite total energy and zero average power (a single pulse), while a power signal has finite average power and infinite energy (any periodic signal); a signal can also be neither. Every signal splits into even and odd parts, and three building blocks recur everywhere: the unit impulse δ(t), the unit step u(t), and the complex exponential e^(jωt)."
            ),
            SubjectTopic(
                "LTI Systems & Convolution",
                "A system is linear if it obeys superposition, and time-invariant if a shift in the input merely shifts the output. An LTI system is completely described by its impulse response h(t): feed it an impulse, and the output tells you everything. The response to any input is the convolution of the input with h(t) — flip h, slide it across x, multiply and integrate at each shift. Convolution is commutative, associative and distributive, which is why block diagrams can be rearranged so freely. Two properties get checked constantly: the system is causal when h(t) = 0 for t < 0 (it cannot respond before the input arrives), and BIBO stable when the impulse response is absolutely integrable — ∫|h(t)| dt < ∞. The step response is simply the running integral of h(t)."
            ),
            SubjectTopic(
                "Fourier Series",
                "Any periodic signal satisfying the (very forgiving) Dirichlet conditions can be rebuilt as a sum of harmonically related sinusoids — or, more compactly, complex exponentials e^(jkω₀t). The coefficients Ck are found by integrating the signal against each harmonic over one period; they form the signal's discrete line spectrum. Symmetry halves the work: an even signal needs only cosines, an odd signal only sines, and half-wave symmetry kills the even harmonics. Parseval's theorem closes the loop between domains: the average power of the signal equals the sum of |Ck|² over all harmonics. Watch a square wave being rebuilt term by term and you will meet the Gibbs phenomenon — a stubborn ~9% overshoot at each jump that narrows as you add terms but never shrinks in height."
            ),
            SubjectTopic(
                "Fourier Transform",
                "Let the period of a periodic signal grow without bound and the Fourier series becomes the Fourier transform: a continuous spectrum X(jω) for aperiodic signals. The same properties that made the series useful survive, and deepen — linearity, time shifting (a delay multiplies the spectrum by e^(−jωt₀), adding linear phase), frequency shifting, and the scaling law that squeezing a signal in time stretches it in frequency. Most important of all is duality with convolution: convolution in time is multiplication in frequency, and multiplication in time is convolution in frequency — the reason filtering is designed in the frequency domain. Classic pairs worth memorising: a rectangular pulse transforms to a sinc, an impulse to a flat spectrum, and the step to πδ(ω) + 1/jω."
            ),
            SubjectTopic(
                "Laplace Transform & ROC",
                "The Laplace transform generalises Fourier by analysing signals with complex frequency s = σ + jω, which lets it handle signals the Fourier transform cannot — anything multiplied by a decaying or growing exponential weight e^(−σt). Every transform comes paired with its region of convergence (ROC), the strip or half-plane of s where the integral converges: the ROC never contains a pole, a right-sided (causal) signal has an ROC to the right of its rightmost pole, and an LTI system is stable exactly when its ROC includes the jω axis. In practice the Laplace transform turns differential equations into algebra: the transfer function H(s) = Y(s)/X(s) captures the whole system, and the initial- and final-value theorems read the signal's start and end straight from H(s)."
            ),
            SubjectTopic(
                "Sampling & Aliasing",
                "Sampling converts a continuous signal into a sequence by recording its value every T seconds. In the frequency domain, sampling copies the signal's spectrum to every multiple of the sampling frequency fs. If those copies overlap, the high frequencies fold into the baseband and masquerade as lower frequencies — aliasing — and once it has happened, no processing can undo it. The Nyquist criterion prevents it: sample at least twice the highest frequency present, fs ≥ 2fmax. Real systems put an anti-alias low-pass filter before the sampler and sample comfortably above the minimum rate. An ideal low-pass filter then reconstructs the original exactly by keeping only the baseband copy and interpolating with sinc functions."
            ),
            SubjectTopic(
                "Z-Transform",
                "The z-transform is the Laplace transform's twin for discrete-time signals: X(z) = Σ x[n] z^(−n), summed over all n. Its region of convergence is a ring (annulus) in the z-plane: a causal sequence converges outside its outermost pole, and a discrete LTI system is stable exactly when the ROC includes the unit circle |z| = 1. Difference equations — the discrete cousins of differential equations — become algebra under the z-transform, which is how digital filters are designed and analysed. Setting z = e^(jω) on the unit circle gives the discrete-time Fourier transform, and the substitution z = e^(sT) shows exactly how the s-plane's left half folds inside the unit circle when a system is sampled."
            ),
            SubjectTopic(
                "DFT & FFT Introduction",
                "Computers cannot hold a continuous spectrum, so the discrete Fourier transform (DFT) samples it: given N samples of a signal, the DFT returns N frequency samples, and the inverse DFT rebuilds the signal exactly. Computed directly it needs N² operations — far too slow for large N. The fast Fourier transform (FFT) exploits the DFT's symmetries (radix-2 decimation in time or frequency) to do the same job in N log₂ N operations, one of the most consequential algorithms ever published. Two practical warnings: a finite observation window smears sharp spectral lines (spectral leakage, tamed by window functions like Hann), and zero-padding a signal interpolates its spectrum more finely without adding true resolution. The FFT also makes convolution fast: transform, multiply, inverse-transform."
            )
        )
    )

    private val localFlashcardDecks: Map<String, List<Flashcard>> = mapOf(
        "analog-circuits" to listOf(
            Flashcard(
                "What is the Q-point of a transistor?",
                "Its DC operating point — the (IC, VCE) pair with no signal applied. Set by the biasing network, it should sit near the middle of the load line so the signal can swing both ways without clipping."
            ),
            Flashcard(
                "Define CMRR of an op-amp.",
                "Common-Mode Rejection Ratio = Ad / Acm: how much better the op-amp amplifies a difference signal than a common-mode signal. Quoted in dB — a 741 offers roughly 90 dB."
            ),
            Flashcard(
                "What is slew rate?",
                "The maximum rate of change of the output voltage, measured in V/µs. It limits how large and how fast an output swing can be before the waveform distorts."
            ),
            Flashcard(
                "What is the gain–bandwidth product?",
                "For a compensated op-amp, gain × bandwidth is constant. With a GBP of 1 MHz, setting a gain of 100 leaves only 10 kHz of usable bandwidth."
            ),
            Flashcard(
                "What is the Early effect?",
                "Base-width modulation: as VCE rises, the collector depletion region widens and the effective base narrows, so IC rises slightly. It gives the output characteristics their upward slope — a finite output resistance ro ≈ VA/IC."
            ),
            Flashcard(
                "Ripple factor — half-wave vs full-wave rectifier?",
                "The ratio of RMS ripple to DC output. Half-wave ≈ 1.21; full-wave ≈ 0.48. Lower is smoother — one reason full-wave rectification wins."
            ),
            Flashcard(
                "State the Barkhausen criterion.",
                "An oscillator sustains oscillation when the loop gain |Aβ| equals 1 and the total phase shift around the loop is 0° (or 360°) at the oscillation frequency."
            ),
            Flashcard(
                "What is input offset voltage?",
                "The small differential input voltage (typically a few mV) that must be applied to bring the op-amp's output exactly to zero — caused by mismatch between the two sides of the input differential pair."
            ),
            Flashcard(
                "Zener vs avalanche breakdown — the difference?",
                "Zener breakdown is quantum tunnelling across a heavily doped junction, dominant below ~5 V, with a negative temperature coefficient. Avalanche breakdown is impact ionisation in a lightly doped junction, dominant above ~7 V, with a positive temperature coefficient."
            ),
            Flashcard(
                "How does negative feedback affect bandwidth?",
                "It widens the bandwidth by the same factor (1 + Aβ) by which it reduces the gain — the gain–bandwidth product stays roughly constant."
            ),
            Flashcard(
                "What is a differential pair?",
                "Two matched transistors sharing one tail current source. It amplifies the difference between its two inputs and rejects signals common to both — it is the input stage of every op-amp."
            ),
            Flashcard(
                "What is clipping?",
                "Flattening of the output waveform when the amplifier tries to swing beyond its supply rails — or when the Q-point is placed too close to saturation or cutoff. A classic source of distortion."
            )
        ),
        "digital-electronics" to listOf(
            Flashcard(
                "State De Morgan's laws.",
                "(A·B)′ = A′ + B′ and (A + B)′ = A′·B′. In words: complementing a product gives the sum of the complements, and complementing a sum gives the product of the complements. Remember it as 'break the bar, change the operator'."
            ),
            Flashcard(
                "How do you negate a number in two's complement?",
                "Invert every bit and add 1. Example: +6 = 0110 → invert → 1001 → add 1 → 1010 = −6. The same operation negates back, which is why one adder circuit handles both addition and subtraction."
            ),
            Flashcard(
                "Which gates are 'universal', and why?",
                "NAND and NOR. Each one alone can build NOT (tie the inputs together), AND and OR (by De Morgan), so any logic function at all can be built from just NAND gates or just NOR gates — handy because both are the cheapest gates in CMOS."
            ),
            Flashcard(
                "Setup time vs hold time — what's the difference?",
                "Setup time is how long the data must be stable BEFORE the clock edge; hold time is how long it must remain stable AFTER the edge. Violate either and the flip-flop may capture the wrong value or go metastable."
            ),
            Flashcard(
                "What is the race-around condition?",
                "In a level-clocked JK flip-flop with J = K = 1, the output toggles again and again for as long as the clock stays high (when the clock pulse outlasts the propagation delay), so the final state is unpredictable. Edge triggering or a master–slave JK eliminates it."
            ),
            Flashcard(
                "What is Gray code, and where is it used?",
                "A binary code in which consecutive values differ in exactly one bit. Used in rotary position encoders and K-map ordering, so a change can never be misread as a wild intermediate value."
            ),
            Flashcard(
                "What is fan-out?",
                "The maximum number of standard gate inputs that one gate output can drive while keeping its voltage levels in specification. Exceed it and the logic levels sag until downstream gates misread them."
            ),
            Flashcard(
                "What is propagation delay, and why does it matter?",
                "The time between an input change and the resulting output change (tPLH / tPHL, averaged). It sets the maximum clock speed of a design, and in ripple counters the delays of every stage add up — the reason synchronous counters win at speed."
            ),
            Flashcard(
                "BCD vs plain binary — what changes?",
                "BCD encodes each decimal digit separately in 4 bits, wasting the codes 1010–1111. So 99 is 1001 1001 (8 bits) in BCD but 1100011 (7 bits) in binary. BCD costs bits but matches decimal displays and counters directly."
            ),
            Flashcard(
                "Latch vs flip-flop — the one-line difference?",
                "A latch is level-sensitive: its output can change whenever the enable is active. A flip-flop is edge-triggered: it samples the input only at the clock edge and holds it for the rest of the cycle."
            ),
            Flashcard(
                "What is a counter's modulus?",
                "The number of distinct states in its counting cycle. A decade counter is mod-10. A mod-n counter needs at least ⌈log₂ n⌉ flip-flops; three flip-flops give at most mod-8."
            ),
            Flashcard(
                "An 8-bit ADC has a 5 V full-scale range. What is its resolution?",
                "One LSB = VFS / 2^n = 5 / 256 ≈ 19.5 mV — the smallest input change it can represent. The quantisation error is at most half of that, ±9.8 mV."
            )
        ),
        "signals-and-systems" to listOf(
            Flashcard(
                "What is the Nyquist rate?",
                "Twice the highest frequency present in the signal (2 fmax). Sample at or above it and the original can, in principle, be reconstructed perfectly; sample below it and aliasing corrupts the signal irreversibly."
            ),
            Flashcard(
                "How are the impulse response and step response related?",
                "The step response is the running integral of the impulse response (cumulative sum, in discrete time); the impulse response is the step response's derivative (or difference). Know either one and you know the LTI system."
            ),
            Flashcard(
                "When is an LTI system causal? When is it stable?",
                "Causal when its impulse response is zero before the input arrives: h(t) = 0 for t < 0. BIBO stable when the impulse response is absolutely integrable (∫|h| dt < ∞; in discrete time, Σ|h[n]| < ∞)."
            ),
            Flashcard(
                "State Parseval's theorem.",
                "Energy (or power) is the same in both domains. For a periodic signal, average power = Σ |Ck|² over all harmonics; for an energy signal, ∫|x(t)|² dt = (1/2π) ∫|X(jω)|² dω."
            ),
            Flashcard(
                "Two rules that govern every ROC.",
                "The region of convergence never contains a pole. And a right-sided (causal) signal has its ROC to the right of the outermost pole (in the z-plane: outside the outermost pole, as a ring bounded by poles)."
            ),
            Flashcard(
                "What does delaying a signal do to its spectrum?",
                "Almost nothing visible: x(t − t₀) transforms to e^(−jωt₀) X(jω). The magnitude spectrum is unchanged; the delay appears only as extra phase that grows linearly with frequency."
            ),
            Flashcard(
                "Energy signal or power signal — how do you tell?",
                "Compute both totals. A signal with finite energy (E < ∞) and zero average power is an energy signal — e.g. a single decaying pulse. Finite average power with infinite energy makes a power signal — e.g. a sinusoid. Some signals are neither."
            ),
            Flashcard(
                "What is the Gibbs phenomenon?",
                "When a Fourier series approximates a jump discontinuity, the partial sums overshoot the jump by about 9% — no matter how many terms you add. Adding terms squeezes the ringing closer to the jump but never reduces the overshoot's height."
            ),
            Flashcard(
                "What is aliasing?",
                "When a signal is sampled below its Nyquist rate, its spectral copies overlap and high-frequency components fold down, appearing as false lower frequencies in the sampled data. It cannot be filtered out afterwards — hence anti-alias filters before the sampler."
            ),
            Flashcard(
                "Write the convolution integral and say what it computes.",
                "y(t) = ∫ x(τ) h(t − τ) dτ. It gives the output of an LTI system: the input x, weighted and summed against flipped, shifted copies of the system's impulse response h."
            ),
            Flashcard(
                "What are the eigenfunctions of an LTI system?",
                "Complex exponentials. Feed e^(jωt) into an LTI system and the output is the same exponential, merely scaled by the complex number H(jω) — the frequency response. No other family of signals passes through unchanged in shape."
            ),
            Flashcard(
                "What is bandwidth?",
                "The width of the frequency band (in hertz) over which a signal or system carries significant content — for a baseband signal, simply its highest significant frequency. It sets both the minimum sampling rate (2×BW) and, for a system, roughly its speed: rise time ≈ 0.35 / bandwidth."
            )
        )
    )

    fun subjectTopics(subjectId: String): List<SubjectTopic> =
        localSubjectTopics[subjectId] ?: emptyList()

    fun flashcards(subjectId: String): List<Flashcard> =
        localFlashcardDecks[subjectId] ?: emptyList()

    // ------------------------------------------------------------------
    // Live caches — filled by sync() from the local store. Screens read
    // through the functions below: a collection the admin has published
    // shows its stored list (tracked in storedCollections, so even an
    // emptied list stays empty); anything else shows built-in content.
    // ------------------------------------------------------------------
    private val examCache = mutableStateListOf<Exam>()
    private val projectCache = mutableStateListOf<Project>()
    private val internshipCache = mutableStateListOf<Internship>()
    private val hackathonCache = mutableStateListOf<Hackathon>()
    private val bookCache = mutableStateListOf<Book>()
    private val subjectCache = mutableStateListOf<StudySubject>()

    fun exams(): List<Exam> =
        if ("exams" in storedCollections) examCache.toList() else localExams
    fun projects(): List<Project> =
        if ("projects" in storedCollections) projectCache.toList() else localProjects
    fun internships(): List<Internship> =
        if ("internships" in storedCollections) internshipCache.toList() else localInternships
    fun hackathons(): List<Hackathon> =
        if ("hackathons" in storedCollections) hackathonCache.toList() else localHackathons
    fun books(): List<Book> =
        if ("books" in storedCollections) bookCache.toList() else localBooks
    fun subjects(): List<StudySubject> =
        if ("subjects" in storedCollections) subjectCache.toList() else localSubjects

    fun exam(id: String) = exams().find { it.id == id }
    fun project(id: String) = projects().find { it.id == id }
    fun internship(id: String) = internships().find { it.id == id }
    fun hackathon(id: String) = hackathons().find { it.id == id }
    fun book(id: String) = books().find { it.id == id }
    fun subject(id: String) = subjects().find { it.id == id }

    // ------------------------------------------------------------------
    // Batch 14 (Offline Core) — the local store. sync() loads the
    // admin-published lists from the on-device SQLite store (AppDb /
    // LocalContent) into the caches above. A collection WITH a stored
    // list shows exactly that list — even an empty one (the admin
    // deleted everything); a collection with NO stored list shows the
    // built-in content. No network is involved anywhere.
    // ------------------------------------------------------------------
    private val storedCollections = mutableSetOf<String>()
    private var appContext: Context? = null

    private val COLLECTIONS = listOf(
        "exams", "projects", "internships", "hackathons", "books", "subjects"
    )

    /** Loads every locally published list into the caches. Called on
     *  app start; instant and fully offline. */
    fun sync(context: Context) {
        appContext = context.applicationContext
        COLLECTIONS.forEach { reloadCollection(context, it) }
    }

    private fun reloadCollection(context: Context, collection: String) {
        val stored = LocalContent.loadList(context, collection)
        if (stored == null) {
            storedCollections.remove(collection)
            return
        }
        storedCollections.add(collection)
        when (collection) {
            "exams" -> {
                examCache.clear()
                examCache.addAll(stored.mapNotNull { examFromMap(it) })
            }
            "projects" -> {
                projectCache.clear()
                projectCache.addAll(stored.mapNotNull { projectFromMap(it) })
            }
            "internships" -> {
                internshipCache.clear()
                internshipCache.addAll(stored.mapNotNull { internshipFromMap(it) })
            }
            "hackathons" -> {
                hackathonCache.clear()
                hackathonCache.addAll(stored.mapNotNull { hackathonFromMap(it) })
            }
            "books" -> {
                bookCache.clear()
                bookCache.addAll(stored.mapNotNull { bookFromMap(it) })
            }
            "subjects" -> {
                subjectCache.clear()
                subjectCache.addAll(stored.mapNotNull { subjectFromMap(it) })
            }
        }
    }

    // ---- Field-map conversion. The admin forms have always produced
    // ---- maps with these keys; the same maps are what LocalContent
    // ---- stores as JSON, so forms and screens never changed.
    private fun str(m: Map<String, Any>, key: String): String =
        m[key] as? String ?: ""

    private fun intOf(m: Map<String, Any>, key: String): Int =
        (m[key] as? Number)?.toInt() ?: 0

    private fun strList(m: Map<String, Any>, key: String): List<String> =
        (m[key] as? List<*>)?.map { it.toString() } ?: emptyList()

    private fun examFromMap(m: Map<String, Any>): Exam? {
        val name = str(m, "name")
        if (name.isBlank()) return null
        return Exam(
            id = str(m, "id"), name = name, category = str(m, "category"),
            overview = str(m, "overview"), eligibility = str(m, "eligibility"),
            pattern = str(m, "pattern"), syllabus = str(m, "syllabus"),
            dates = str(m, "dates"), papers = str(m, "papers"),
            cutoff = str(m, "cutoff")
        )
    }

    private fun projectFromMap(m: Map<String, Any>): Project? {
        val title = str(m, "title")
        if (title.isBlank()) return null
        return Project(
            id = str(m, "id"), title = title,
            level = str(m, "level").ifBlank { "Basic" },
            domain = str(m, "domain"), time = str(m, "time"),
            cost = str(m, "cost"), overview = str(m, "overview"),
            components = strList(m, "components"), steps = strList(m, "steps"),
            learns = str(m, "learns")
        )
    }

    private fun internshipFromMap(m: Map<String, Any>): Internship? {
        val title = str(m, "title")
        if (title.isBlank()) return null
        return Internship(
            id = str(m, "id"), title = title, org = str(m, "org"),
            type = str(m, "type"), paid = str(m, "paid"),
            location = str(m, "location"), eligibility = str(m, "eligibility"),
            deadline = str(m, "deadline"), description = str(m, "description"),
            link = str(m, "link")
        )
    }

    private fun hackathonFromMap(m: Map<String, Any>): Hackathon? {
        val name = str(m, "name")
        if (name.isBlank()) return null
        return Hackathon(
            id = str(m, "id"), name = name, org = str(m, "org"),
            deadline = str(m, "deadline"), mode = str(m, "mode"),
            location = str(m, "location"), prize = str(m, "prize"),
            description = str(m, "description"), link = str(m, "link")
        )
    }

    private fun bookFromMap(m: Map<String, Any>): Book? {
        val title = str(m, "title")
        if (title.isBlank()) return null
        return Book(
            id = str(m, "id"), title = title, author = str(m, "author"),
            format = str(m, "format").ifBlank { "PDF" },
            rating = str(m, "rating"), cover = str(m, "cover")
        )
    }

    private fun subjectFromMap(m: Map<String, Any>): StudySubject? {
        val name = str(m, "name")
        if (name.isBlank()) return null
        return StudySubject(
            id = str(m, "id"), name = name,
            progressPercent = intOf(m, "progressPercent"),
            topicsDone = intOf(m, "topicsDone"),
            topicsTotal = intOf(m, "topicsTotal"),
            nextTopic = str(m, "nextTopic")
        )
    }

    private fun examToMap(e: Exam): Map<String, Any> = mapOf(
        "id" to e.id, "name" to e.name, "category" to e.category,
        "overview" to e.overview, "eligibility" to e.eligibility,
        "pattern" to e.pattern, "syllabus" to e.syllabus,
        "dates" to e.dates, "papers" to e.papers, "cutoff" to e.cutoff
    )

    private fun projectToMap(p: Project): Map<String, Any> = mapOf(
        "id" to p.id, "title" to p.title, "level" to p.level,
        "domain" to p.domain, "time" to p.time, "cost" to p.cost,
        "overview" to p.overview, "components" to p.components,
        "steps" to p.steps, "learns" to p.learns
    )

    private fun internshipToMap(i: Internship): Map<String, Any> = mapOf(
        "id" to i.id, "title" to i.title, "org" to i.org, "type" to i.type,
        "paid" to i.paid, "location" to i.location,
        "eligibility" to i.eligibility, "deadline" to i.deadline,
        "description" to i.description, "link" to i.link
    )

    private fun hackathonToMap(h: Hackathon): Map<String, Any> = mapOf(
        "id" to h.id, "name" to h.name, "org" to h.org,
        "deadline" to h.deadline, "mode" to h.mode, "location" to h.location,
        "prize" to h.prize, "description" to h.description, "link" to h.link
    )

    private fun bookToMap(b: Book): Map<String, Any> = mapOf(
        "id" to b.id, "title" to b.title, "author" to b.author,
        "format" to b.format, "rating" to b.rating, "cover" to b.cover
    )

    private fun subjectToMap(s: StudySubject): Map<String, Any> = mapOf(
        "id" to s.id, "name" to s.name,
        "progressPercent" to s.progressPercent, "topicsDone" to s.topicsDone,
        "topicsTotal" to s.topicsTotal, "nextTopic" to s.nextTopic
    )

    /** The collection's current effective list as field maps: the
     *  stored list when one exists, else the built-in list converted.
     *  Admin writes always start from this full list (full-list rule),
     *  so publishing one new item never makes the others vanish. */
    private fun currentMaps(context: Context, collection: String): List<Map<String, Any>> {
        LocalContent.loadList(context, collection)?.let { return it }
        return when (collection) {
            "exams" -> localExams.map { examToMap(it) }
            "projects" -> localProjects.map { projectToMap(it) }
            "internships" -> localInternships.map { internshipToMap(it) }
            "hackathons" -> localHackathons.map { hackathonToMap(it) }
            "books" -> localBooks.map { bookToMap(it) }
            "subjects" -> localSubjects.map { subjectToMap(it) }
            else -> emptyList()
        }
    }

    // ------------------------------------------------------------------
    // Admin writes — to the on-device SQLite store.
    // ------------------------------------------------------------------
    fun addDoc(collection: String, fields: Map<String, Any>, onDone: (Boolean) -> Unit) {
        val context = appContext
        if (context == null) {
            onDone(false)
            return
        }
        val ok = runCatching {
            val list = currentMaps(context, collection).toMutableList()
            list.add(fields + ("id" to java.util.UUID.randomUUID().toString()))
            LocalContent.saveList(context, collection, list)
            reloadCollection(context, collection)
            true
        }.getOrDefault(false)
        onDone(ok)
    }

    fun deleteDoc(collection: String, id: String) {
        // Instant feedback in the manage list, exactly as before.
        when (collection) {
            "exams" -> examCache.removeAll { it.id == id }
            "projects" -> projectCache.removeAll { it.id == id }
            "internships" -> internshipCache.removeAll { it.id == id }
            "hackathons" -> hackathonCache.removeAll { it.id == id }
            "books" -> bookCache.removeAll { it.id == id }
            "subjects" -> subjectCache.removeAll { it.id == id }
        }
        val context = appContext ?: return
        runCatching {
            val list = currentMaps(context, collection)
                .filterNot { it["id"] == id }
            LocalContent.saveList(context, collection, list)
            reloadCollection(context, collection)
        }
    }

    /** Panel sign-in: checks the LOCAL account store and opens the
     *  session. Whether the account may publish is a separate check —
     *  UserProfile.isAdmin reads the account's admin flag. */
    fun adminLogin(
        context: Context,
        email: String,
        password: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        val account = LocalAccounts.verify(context, email.trim(), password)
        if (account == null) {
            onDone(false, "Wrong email or password — please check and try again.")
        } else {
            UserProfile.setSession(context, account.email)
            onDone(true, null)
        }
    }

    fun adminLogout(context: Context) {
        UserProfile.clearSession(context)
    }
}
