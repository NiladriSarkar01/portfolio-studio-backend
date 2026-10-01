package com.portfolio.service;

import com.portfolio.model.CommandResponse;
import com.portfolio.model.Profile;
import com.portfolio.model.Project;
import com.portfolio.model.Resume;
import com.portfolio.model.Skill;
import com.portfolio.model.SkillsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processes terminal commands received from the React frontend.
 * Each command maps to a CommandResponse with ANSI-escaped output.
 *
 * Nothing about the owner is hardcoded here. Every command that prints portfolio data
 * reads the same JSON-backed services as the REST API (profile, skills, projects, resume),
 * so the terminal and the UI can never disagree.
 *
 * ANSI color shortcuts used:
 *   RED    = \u001B[38;5;197m
 *   GREEN  = \u001B[38;5;46m
 *   AMBER  = \u001B[38;5;214m
 *   CYAN   = \u001B[38;5;51m
 *   MUTED  = \u001B[38;5;67m
 *   WHITE  = \u001B[97m
 *   RESET  = \u001B[0m
 *   BOLD   = \u001B[1m
 */
@Service
@RequiredArgsConstructor
public class CommandService {

    // Single source of truth: the same JSON-backed services the REST controllers use.
    private final PortfolioDataService data;
    private final ProjectService projectService;
    private final ResumeService resumeService;

    // ANSI codes
    private static final String RED   = "\u001B[38;5;197m";
    private static final String GREEN = "\u001B[38;5;46m";
    private static final String AMBER = "\u001B[38;5;214m";
    private static final String CYAN  = "\u001B[38;5;51m";
    private static final String MUTED = "\u001B[38;5;67m";
    private static final String WHITE = "\u001B[97m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";

    private String r(String s) { return RED   + s + RESET; }
    private String g(String s) { return GREEN + s + RESET; }
    private String a(String s) { return AMBER + s + RESET; }
    private String c(String s) { return CYAN  + s + RESET; }
    private String m(String s) { return MUTED + s + RESET; }
    private String w(String s) { return WHITE + s + RESET; }

    public CommandResponse execute(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            return new CommandResponse("");
        }

        String cmd   = rawCommand.trim().toLowerCase();
        String[] parts = cmd.split("\\s+");
        String base  = parts[0];

        // Route to handler
        return switch (base) {
            case "help"     -> help();
            case "whoami"   -> whoami();
            case "ls"       -> ls(parts);
            case "cat"      -> cat(parts);
            case "pwd"      -> new CommandResponse(w("/home/root"));
            case "echo"     -> echo(rawCommand);
            case "uname"    -> uname();
            case "ifconfig", "ip" -> ifconfig();
            case "nmap"     -> nmap(parts);
            case "htop"     -> htop();
            case "date"     -> date();
            case "open"     -> open(parts);
            case "github"   -> github();
            case "linkedin" -> linkedin();
            case "blog"     -> blog();
            case "skills"   -> CommandResponse.openApp(g("Opening skills matrix..."), "skills");
            case "projects" -> CommandResponse.openApp(g("Opening projects..."), "projects");
            case "resume"   -> CommandResponse.openApp(g("Opening resume..."), "resume");
            case "contact"  -> CommandResponse.openApp(g("Opening contact form..."), "contact");
            case "about"    -> CommandResponse.openApp(g("Opening about..."), "about");
            case "clear"    -> new CommandResponse("__CLEAR__");
            case "exit", "logout" -> new CommandResponse(a("Session terminated. Goodbye, root."));
            default         -> notFound(rawCommand.trim());
        };
    }

    // ── Handlers ────────────────────────────────────────────────────────

    private CommandResponse help() {
        String out = a(BOLD + "Available Commands" + RESET) + "\r\n" +
            "  " + c("whoami")   + "              " + m("Who is the owner") + "\r\n" +
            "  " + c("ls")       + "              " + m("List portfolio sections") + "\r\n" +
            "  " + c("cat")      + " " + w("[file]") + "       " + m("Read: skills | projects | resume | contact") + "\r\n" +
            "  " + c("pwd")      + "              " + m("Print working directory") + "\r\n" +
            "  " + c("open")     + " " + w("[app]") + "        " + m("Open window: about projects resume contact skills") + "\r\n" +
            "  " + c("nmap self")+ "              " + m("Scan the portfolio host") + "\r\n" +
            "  " + c("htop")     + "              " + m("Show running processes") + "\r\n" +
            "  " + c("ifconfig") + "              " + m("Network interfaces") + "\r\n" +
            "  " + c("uname -a") + "              " + m("Kernel / system info") + "\r\n" +
            "  " + c("date")     + "              " + m("Current date and time") + "\r\n" +
            "  " + c("github")   + "              " + m("Open GitHub profile") + "\r\n" +
            "  " + c("linkedin") + "              " + m("Open LinkedIn profile") + "\r\n" +
            "  " + c("blog")     + "              " + m("Open personal blog") + "\r\n" +
            "  " + c("clear")    + "              " + m("Clear the terminal") + "\r\n" +
            "  " + c("exit")     + "              " + m("Close session");
        return new CommandResponse(out);
    }

    private CommandResponse whoami() {
        Profile p = data.profile();
        Profile.Stats st = p.stats();
        String out =
                w(p.username()) + "\r\n" +
                m("Name      : ") + w(p.name()) + "\r\n" +
                m("Role      : ") + w(p.speciality()) + "\r\n" +
                m("Education : ") + w(st.education()) + "\r\n" +
                m("College   : ") + w(p.college()) + "\r\n" +
                m("Location  : ") + w(st.location()) + "\r\n" +
                m("Focus     : ") + w(st.focus()) + "\r\n" +
                m("Status    : ") + g(st.status()) + "\r\n" +
                m("GitHub    : ") + c(p.github()) + "\r\n" +
                m("Portfolio : ") + c("You're already in it 😉");

        return new CommandResponse(out);
    }

    private CommandResponse ls(String[] parts) {
        String out = c("drwxr-xr-x") + "  " + g("about/")    + "      " + m("Who I am") + "\r\n" +
                     c("drwxr-xr-x") + "  " + g("projects/") + "   " + m("My work") + "\r\n" +
                     c("drwxr-xr-x") + "  " + g("resume/")   + "     " + m("My experience") + "\r\n" +
                     c("drwxr-xr-x") + "  " + g("contact/")  + "    " + m("Get in touch") + "\r\n" +
                     c("-rwxr-xr-x") + "  " + w("skills.json") + "  " + m("Skill matrix") + "\r\n" +
                     c("-rwxr-xr-x") + "  " + w("terminal")   + "    " + m("You are here");
        return new CommandResponse(out);
    }

    private CommandResponse cat(String[] parts) {
        if (parts.length < 2) {
            return new CommandResponse(r("cat: missing operand. Usage: cat [skills|projects|resume|contact]"));
        }
        return switch (parts[1]) {
            case "skills"   -> catSkills();
            case "projects" -> catProjects();
            case "resume"   -> catResume();
            case "contact"  -> catContact();
            default -> new CommandResponse(r("cat: " + parts[1] + ": No such file or directory"));
        };
    }

    private CommandResponse catSkills() {
        SkillsData sd = data.skills();
        StringBuilder out = new StringBuilder(a("[SKILL MATRIX]")).append("\r\n");

        for (Map.Entry<String, List<Skill>> cat : sd.skills().entrySet()) {
            out.append(c(cat.getKey().toUpperCase())).append("\r\n");
            for (Skill skill : cat.getValue()) {
                if (skill.level() != null) {
                    out.append(formatSkill(skill.name(), skillBar(skill.level()), skill.level()));
                } else {
                    out.append(m(skill.name())).append("\r\n");
                }
            }
        }

        out.append(c("TOOLS")).append("\r\n")
           .append(m(String.join(", ", sd.tools()))).append("\r\n");
        return new CommandResponse(out.toString());
    }

    /** 20-cell bar, e.g. 85% -> 17 filled cells. */
    private String skillBar(int pct) {
        int filled = Math.max(0, Math.min(20, Math.round(pct / 5f)));
        return "█".repeat(filled) + "░".repeat(20 - filled);
    }

    private String formatSkill(String name, String bar, int pct) {
        return String.format("%-50s %s %s%d%%%s\r\n",
            m(name), g(bar), RED, pct, RESET);
    }

    private CommandResponse catProjects() {
        Profile p = data.profile();
        StringBuilder out = new StringBuilder(a("[/home/" + p.username() + "/projects]")).append("\r\n");

        int i = 1;
        for (Project pr : projectService.getAll()) {
            out.append(String.format("%02d. ", i++)).append(c(pr.getName()));
            if (pr.getCategory() != null && !pr.getCategory().isBlank()) {
                out.append("  ").append(m("[" + pr.getCategory() + "]"));
            }
            out.append("\r\n").append("    ").append(m(pr.getDescription())).append("\r\n");
        }

        out.append(m("Tip: type ")).append(c("open projects"))
           .append(m(" to browse screenshots, architecture and source code."));
        return new CommandResponse(out.toString());
    }

    private CommandResponse catResume() {
        Profile p = data.profile();
        Resume res = resumeService.get();
        StringBuilder out = new StringBuilder();

        out.append(a("[resume.txt]")).append("\r\n")
           .append(w(p.name())).append("  —  ").append(r(p.speciality())).append("\r\n")
           .append(m(p.stats().location() + " · " + p.openTo())).append("\r\n")
           .append(c("EXPERIENCE")).append("\r\n");

        for (Resume.Experience e : res.experience()) {
            out.append("  ").append(w(e.title())).append("  ")
               .append(m("· " + e.company() + " · " + e.period())).append("\r\n");
            if (e.points() != null) {
                for (String point : e.points()) {
                    out.append("     ").append(m("- " + point)).append("\r\n");
                }
            }
        }

        out.append(c("EDUCATION")).append("\r\n");
        for (Resume.Education e : res.education()) {
            out.append("  ").append(w(e.degree())).append("  ")
               .append(m("· " + e.institution() + " · " + e.period())).append("\r\n");
            if (e.notes() != null && !e.notes().isBlank()) {
                out.append("     ").append(m(e.notes())).append("\r\n");
            }
        }

        out.append(c("CERTIFICATIONS")).append("\r\n");
        for (Resume.Certification cert : res.certifications()) {
            String year = (cert.year() == null || cert.year().isBlank()) ? "" : " · " + cert.year();
            out.append("  ").append(w(cert.name())).append("  ")
               .append(m("· " + cert.issuer() + year)).append("\r\n");
        }

        out.append(c("TECH STACK")).append("\r\n");
        for (Map.Entry<String, List<Skill>> cat : data.skills().skills().entrySet()) {
            String names = cat.getValue().stream()
                    .map(sk -> g("✓") + " " + sk.name())
                    .collect(Collectors.joining("   "));
            out.append("  ").append(m(cat.getKey() + ": ")).append(names).append("\r\n");
        }

        out.append(m("Type ")).append(c("open resume")).append(m(" to view the full resume."));
        return new CommandResponse(out.toString());
    }

    private CommandResponse catContact() {
        Profile p = data.profile();
        String out = a("[CONTACT INFO]") + "\r\n" +
                m("Email     : ") + c(p.email()) + "\r\n" +
                m("GitHub    : ") + c(p.github()) + "\r\n" +
                m("LinkedIn  : ") + c(p.linkedin()) + "\r\n" +
                m("Portfolio : ") + c("You are already here :)") + "\n\n" +
                m("Status    : ") + g(p.openTo()) + "\r\n" +
                m("Type ") + c("open contact") + m(" to send a message.");

        return new CommandResponse(out);
    }

    private CommandResponse echo(String raw) {
        String msg = raw.replaceFirst("(?i)^echo\\s*", "");
        return new CommandResponse(w(msg));
    }

    private CommandResponse uname() {
        return new CommandResponse(w("Linux kali-folio 6.11.0-kali3-amd64 #1 SMP PREEMPT_DYNAMIC Kali 6.11.6-1kali1 (2024-11-08) x86_64 GNU/Linux"));
    }

    private CommandResponse ifconfig() {
        String out = c("eth0:") + "\n" +
            m("  inet ") + w("192.168.1.42 ") + m("netmask ") + w("255.255.255.0 ") + m("broadcast ") + w("192.168.1.255") + "\n" +
            m("  ether ") + w("de:ad:be:ef:ca:fe") + m("  txqueuelen 1000  (Ethernet)") + "\n\n" +
            c("lo:") + "\n" +
            m("  inet ") + w("127.0.0.1 ") + m("netmask 255.0.0.0") + "\n" +
            m("  loopback");
        return new CommandResponse(out);
    }

    private CommandResponse nmap(String[] parts) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String out = m("Starting Nmap 7.94 ( https://nmap.org ) at " + now) + "\n" +
            m("Nmap scan report for kali-folio (127.0.0.1)") + "\n" +
            g("Host is up (0.00012s latency).") + "\n\n" +
            a("PORT     STATE  SERVICE    VERSION") + "\n" +
            m("80/tcp   open   http       Portfolio v1.0") + "\n" +
            m("443/tcp  open   https      SSL/TLS") + "\n" +
            m("22/tcp   open   ssh        OpenSSH 9.6") + "\n" +
            m("8080/tcp open   http-alt   Spring Boot API") + "\n\n" +
            g("4 open ports detected. 0 vulnerabilities. Impenetrable.");
        return new CommandResponse(out);
    }

    private CommandResponse htop() {
        String out = a(" CPU[████████░░░░░░░░░░░░ 38%]  MEM[██████░░░░░░░░░░░░░░ 2.1G/8G]") + "\n\n" +
            c("  PID   USER     PRI  CPU%  MEM%  CMD") + "\n" +
            m(" 1337   root      20   12.4   4.2  ") + w("portfolio-api") + "\n" +
            m("  420   root      20    5.1   1.8  ") + w("npm run dev") + "\n" +
            m("  666   root      20    2.3   0.9  ") + w("nmap") + "\n" +
            m("    1   root      20    0.1   0.1  ") + w("systemd");
        return new CommandResponse(out);
    }

    private CommandResponse date() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy"));
        return new CommandResponse(w(now));
    }

    private CommandResponse open(String[] parts) {
        if (parts.length < 2) {
            return new CommandResponse(r("open: missing argument. Usage: open [about|projects|resume|contact|skills]"));
        }
        String app = parts[1];
        if (Map.of("about","about","projects","projects","resume","resume",
                   "contact","contact","skills","skills","files","files").containsKey(app)) {
            return CommandResponse.openApp(g("Opening " + app + "..."), app);
        }
        return new CommandResponse(r("open: unknown app '" + app + "'. Try: about projects resume contact skills"));
    }

    private CommandResponse github() {
        return open(data.profile().github());
    }

    private CommandResponse linkedin() {
        return open(data.profile().linkedin());
    }

    private CommandResponse blog() {
        String blog = data.profile().blog();
        if (blog == null || blog.isBlank()) {
            return new CommandResponse(m("No blog configured. Add a \"blog\" field to profile.json."));
        }
        return open(blog);
    }

    /** Works whether profile.json stores "github.com/x" or "https://github.com/x". */
    private CommandResponse open(String link) {
        String url = link.matches("(?i)^https?://.*") ? link : "https://" + link;
        return new CommandResponse(g("Opening " + url) + "\n" + m("(action handled by the browser)"));
    }

    private CommandResponse notFound(String cmd) {
        return new CommandResponse(
            r("bash: " + cmd + ": command not found") + "\n" +
            m("Type ") + c("help") + m(" to see available commands.")
        );
    }
}
