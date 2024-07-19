package it.unibo.application.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Consumazione;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Ricetta;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;
import it.unibo.application.dto.Valori;
import it.unibo.application.dto.ValoriCibo;
import it.unibo.application.dto.ValoriRicetta;

public class Model {

    private final Connection connection;

    public Model(final Connection connection) {
        this.connection = connection;
    }

    public boolean isValid(String username, char[] password) {
        final String CHECK_USER = "SELECT * FROM UTENTE WHERE Username = ? AND Password = ?";
        try (PreparedStatement s = connection.prepareStatement(CHECK_USER)) {
            s.setString(1, username);
            s.setString(2, String.valueOf(password));
            return s.executeQuery().next();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean registerUser(String username, char[] password) {
        final String ADD_USER = "INSERT INTO UTENTE (Username, Password) VALUES (?, ?)";
        try (PreparedStatement s = connection.prepareStatement(ADD_USER)) {
            s.setString(1, username);
            s.setString(2, String.valueOf(password));
            return s.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    public Optional<BigDecimal> leggiObbiettivo(String username) {
        final String LEGGI_OBBIETTIVO = "SELECT Obbiettivo FROM UTENTE WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_OBBIETTIVO)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            if (!res.next()) {
                throw new IllegalArgumentException("No such user: \"" + username + "\"");
            } else {
                return Optional.ofNullable(res.getObject(1, BigDecimal.class));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Target> leggiTarget(String username) {
        final String LEGGI_TARGET = "SELECT Kcal, PercentualeProteine, PercentualeGrassi, PercentualeCarboidrati FROM UTENTE WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TARGET)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            if (!res.next()) {
                throw new IllegalArgumentException("No such user: \"" + username + "\"");
            } else {
                Optional<Integer> kcal = Optional.ofNullable(res.getObject(1, Integer.class));
                return Optional.ofNullable(kcal.isPresent() ? new Target(
                        kcal.get(),
                        Optional.ofNullable(res.getObject(2, Integer.class)),
                        Optional.ofNullable(res.getObject(3, Integer.class)),
                        Optional.ofNullable(res.getObject(4, Integer.class)))
                        : null);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean impostaTarget(String username, Optional<Target> t) {
        final String IMPOSTA_TARGET = "UPDATE UTENTE SET Kcal = ?, PercentualeProteine = ?, PercentualeGrassi = ?, PercentualeCarboidrati = ? WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(IMPOSTA_TARGET)) {

            if (t.isEmpty()) {
                s.setNull(1, Types.INTEGER);
                s.setNull(2, Types.TINYINT);
                s.setNull(3, Types.TINYINT);
                s.setNull(4, Types.TINYINT);
            } else {
                var target = t.get();
                s.setInt(1, target.kcal());
                if (target.percentualeProteine().isPresent()) {
                    s.setInt(2, target.percentualeProteine().get());
                } else {
                    s.setNull(2, Types.TINYINT);
                }
                if (target.percentualeGrassi().isPresent()) {
                    s.setInt(3, target.percentualeGrassi().get());
                } else {
                    s.setNull(3, Types.TINYINT);
                }
                if (target.percentualeCarboidrati().isPresent()) {
                    s.setInt(4, target.percentualeCarboidrati().get());
                } else {
                    s.setNull(4, Types.TINYINT);
                }
            }
            s.setString(5, username);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean impostaObbiettivo(String username, Optional<BigDecimal> obbiettivo) {
        final String IMPOSTA_TARGET = "UPDATE UTENTE SET Obbiettivo = ? WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(IMPOSTA_TARGET)) {
            if (obbiettivo.isPresent()) {
                s.setObject(1, obbiettivo.orElse(null));
            }
            s.setString(2, username);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public List<Misurazione> leggiMisurazioni(String username) {
        final String LEGGI_MISURAZIONI = "SELECT Data, Peso FROM MISURAZIONE WHERE Username = ? ORDER BY Data DESC";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_MISURAZIONI)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Misurazione> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Misurazione(username, res.getDate(1).toLocalDate(), res.getBigDecimal(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungiMisurazione(Misurazione m) {
        final String AGGIUNGI_MISURAZIONE = "INSERT INTO MISURAZIONE (Data, Peso, Username) VALUES (?,?,?)";
        try (PreparedStatement s = connection.prepareStatement(AGGIUNGI_MISURAZIONE)) {
            s.setDate(1, Date.valueOf(m.data()));
            s.setBigDecimal(2, m.peso());
            s.setString(3, m.username());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean modificaMisurazione(Misurazione m, BigDecimal nuovoPeso) {
        final String MODIFICA_MISURAZIONE = "UPDATE MISURAZIONE SET Peso = ? WHERE Data = ? AND Username = ?";
        try (PreparedStatement s = connection.prepareStatement(MODIFICA_MISURAZIONE)) {
            s.setBigDecimal(1, nuovoPeso);
            s.setDate(2, Date.valueOf(m.data()));
            s.setString(3, m.username());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean eliminaMisurazione(Misurazione m) {
        final String ELIMINA_MISURAZIONE = "DELETE FROM MISURAZIONE WHERE Data = ? AND Username = ?";
        try (PreparedStatement s = connection.prepareStatement(ELIMINA_MISURAZIONE)) {
            s.setDate(1, Date.valueOf(m.data()));
            s.setString(2, m.username());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public List<Tag> leggiTag() {
        final String LEGGI_TAG = "SELECT ParolaChiave, Creatore FROM TAG ORDER BY ParolaChiave";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TAG)) {
            ResultSet res = s.executeQuery();
            List<Tag> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Tag(res.getString(1), res.getString(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Tag> leggiTagUtente(String username) {
        final String LEGGI_TAG = "SELECT ParolaChiave, Creatore FROM TAG WHERE Creatore = ? ORDER BY ParolaChiave";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TAG)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Tag> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Tag(res.getString(1), res.getString(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Tag> leggiTagAlimento(Alimento alimento) {
        final String LEGGI_TAG_ALIMENTO = """
                SELECT a.ParolaChiave, Creatore
                FROM ASSOCIAZIONE a JOIN TAG t ON a.ParolaChiave = t.ParolaChiave
                WHERE CodAlimento = ?
                """;
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TAG_ALIMENTO)) {
            s.setInt(1, alimento.codAlimento());
            ResultSet res = s.executeQuery();
            Set<Tag> ret = new HashSet<>();
            while (res.next()) {
                ret.add(new Tag(res.getString(1), res.getString(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean modificaTagAlimento(Alimento alimento, Set<Tag> vecchi, Set<Tag> nuovi) {
        final String AGGIUNGI_ASSOCIAZIONE = """
                INSERT INTO ASSOCIAZIONE(ParolaChiave, CodAlimento)
                VALUES (?, ?)
                """;
        final String RIMUOVI_ASSOCIAZIONE = """
                DELETE FROM ASSOCIAZIONE
                WHERE ParolaChiave = ?
                AND CodAlimento = ?
                """;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement aggiungi = connection.prepareStatement(AGGIUNGI_ASSOCIAZIONE);
                    PreparedStatement rimuovi = connection.prepareStatement(RIMUOVI_ASSOCIAZIONE)) {

                var daAggiungere = new HashSet<>(nuovi);
                daAggiungere.removeAll(vecchi);
                for (Tag tag : daAggiungere) {
                    aggiungi.setString(1, tag.parolaChiave());
                    aggiungi.setInt(2, alimento.codAlimento());
                    aggiungi.addBatch();
                }
                if (daAggiungere.size() > 0 && Arrays.stream(aggiungi.executeBatch()).anyMatch(i -> i != 1)) {
                    throw new SQLException("Errore durante la rimozione di un'associazione.");
                }
                var daRimuovere = new HashSet<>(vecchi);
                daRimuovere.removeAll(nuovi);
                for (Tag tag : daRimuovere) {
                    rimuovi.setString(1, tag.parolaChiave());
                    rimuovi.setInt(2, alimento.codAlimento());
                    rimuovi.addBatch();
                }
                if (daRimuovere.size() > 0 && Arrays.stream(rimuovi.executeBatch()).anyMatch(i -> i != 1)) {
                    throw new SQLException("Errore durante la rimozione di un'associazione.");
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                System.err.println(e);
                connection.rollback();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungiTag(Tag tag) {
        final String AGGIUNGI_TAG = "INSERT INTO TAG (ParolaChiave, Creatore) VALUES (?,?)";
        try (PreparedStatement s = connection.prepareStatement(AGGIUNGI_TAG)) {
            s.setString(1, tag.parolaChiave());
            s.setString(2, tag.creatore());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean eliminaTag(Tag tag) {
        final String ELIMINA_TAG = "DELETE FROM TAG WHERE ParolaChiave = ? AND Creatore = ?";
        try (PreparedStatement s = connection.prepareStatement(ELIMINA_TAG)) {
            s.setString(1, tag.parolaChiave());
            s.setString(2, tag.creatore());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean aggiungiCibo(Alimento cibo) {
        final String AGGIUNGI_CIBO = "INSERT INTO ALIMENTO(Nome, Kcal, Carboidrati, Grassi, Proteine, Porzione, Tipo, Brand, Proprietario, Privato) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement s = connection.prepareStatement(AGGIUNGI_CIBO)) {
            s.setString(1, cibo.nome());
            s.setInt(2, cibo.kcal());
            s.setInt(3, cibo.carboidrati());
            s.setInt(4, cibo.grassi());
            s.setInt(5, cibo.proteine());
            if (cibo.porzione().isPresent()) {
                s.setInt(6, cibo.porzione().get());
            } else {
                s.setNull(6, Types.INTEGER);
            }
            s.setString(7, String.valueOf(cibo.tipo()));
            if (cibo.brand().isPresent()) {
                s.setString(8, cibo.brand().get());
            } else {
                s.setNull(8, Types.VARCHAR);
            }
            s.setString(9, cibo.proprietario());
            s.setBoolean(10, cibo.privato());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public List<Alimento> alimentiPropri(String username) {
        final String LEGGI_ALIMENTI = "SELECT CodAlimento, Nome, Kcal, Carboidrati, Grassi, Proteine, Porzione, Tipo, Brand, Proprietario, Privato FROM ALIMENTO WHERE Proprietario = ? ORDER BY CodAlimento DESC";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_ALIMENTI)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Alimento> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Alimento(
                        res.getInt(1),
                        res.getString(2),
                        res.getInt(3),
                        res.getInt(4),
                        res.getInt(5),
                        res.getInt(6),
                        Optional.ofNullable(res.getObject(7, Integer.class)),
                        res.getString(8).charAt(0),
                        Optional.ofNullable(res.getObject(9, String.class)),
                        res.getString(10),
                        res.getBoolean(11)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Alimento> cercaAlimenti(String username, Optional<String> search, Optional<Set<Tag>> tags) {
        StringBuilder b = new StringBuilder();
        tags.ifPresentOrElse(t -> {
            b.append(
                    """
                            WITH ALIMENTI_VALIDI AS (
                            SELECT DISTINCT CodAlimento
                            FROM ASSOCIAZIONE
                            WHERE ParolaChiave IN (
                            """);
            b.append("?,".repeat(tags.get().size() - 1));
            b.append("?))");
        }, () -> b.append("""
                WITH ALIMENTI_VALIDI AS
                (SELECT DISTINCT CodAlimento
                FROM ALIMENTO)
                """));

        b.append(
                """
                        SELECT ALIMENTO.CodAlimento, Nome, Kcal, Carboidrati, Grassi, Proteine, Porzione, Tipo, Brand, Proprietario, Privato
                        FROM ALIMENTI_VALIDI JOIN ALIMENTO ON ALIMENTI_VALIDI.CodAlimento = ALIMENTO.CodAlimento
                        WHERE (Proprietario = ? OR Privato = 0)
                        """);

        search.ifPresent(s -> b.append(
                """
                        AND Nome LIKE ?
                                """));
        b.append("""
                        ORDER BY CASE
                            WHEN Proprietario = ? THEN 0
                            ELSE 1
                        END,
                        Nome
                """);

        final String FILTRA_ALIMENTI = b.toString();

        try (PreparedStatement s = connection.prepareStatement(FILTRA_ALIMENTI)) {
            int i = 1;

            if (tags.isPresent()) {
                for (Tag tag : tags.get()) {
                    s.setString(i++, tag.parolaChiave());
                }
            }
            s.setString(i++, username);
            if (search.isPresent()) {
                s.setString(i++, "%" + search.get() + "%");
            }
            s.setString(i++, username);
            ResultSet res = s.executeQuery();

            List<Alimento> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Alimento(
                        res.getInt(1),
                        res.getString(2),
                        res.getInt(3),
                        res.getInt(4),
                        res.getInt(5),
                        res.getInt(6),
                        Optional.ofNullable(res.getObject(7, Integer.class)),
                        res.getString(8).charAt(0),
                        Optional.ofNullable(res.getObject(9, String.class)),
                        res.getString(10),
                        res.getBoolean(11)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungiPreferito(String username, Alimento preferito) {
        final String AGGIUNGI_PREFERITO = """
                INSERT INTO PREFERENZA (Username, CodAlimento)
                VALUES (?, ?)
                """;
        try (PreparedStatement s = connection.prepareStatement(AGGIUNGI_PREFERITO)) {
            s.setString(1, username);
            s.setInt(2, preferito.codAlimento());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public List<Alimento> leggiPreferiti(String username) {
        final String LEGGI_PREFERITI = """
                SELECT a.CodAlimento, Nome, Kcal, Carboidrati, Grassi, Proteine, Porzione, Tipo, Brand, Proprietario, Privato
                FROM PREFERENZA p
                JOIN ALIMENTO a ON p.CodAlimento = a.CodAlimento
                WHERE Username = ?;
                """;
        try (PreparedStatement s = connection.prepareStatement(LEGGI_PREFERITI)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Alimento> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Alimento(
                        res.getInt(1),
                        res.getString(2),
                        res.getInt(3),
                        res.getInt(4),
                        res.getInt(5),
                        res.getInt(6),
                        Optional.ofNullable(res.getObject(7, Integer.class)),
                        res.getString(8).charAt(0),
                        Optional.ofNullable(res.getObject(9, String.class)),
                        res.getString(10),
                        res.getBoolean(11)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean eliminaPreferito(String username, Alimento preferito) {
        final String RIMUOVI_PREFERITO = """
                DELETE FROM PREFERENZA
                WHERE Username = ?
                AND CodAlimento = ?
                """;
        try (PreparedStatement s = connection.prepareStatement(RIMUOVI_PREFERITO)) {
            s.setString(1, username);
            s.setInt(2, preferito.codAlimento());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public List<Consumazione> leggiConsumazioni(String username) {
        final String LEGGI_CONSUMAZIONI = """
                SELECT Username, Numero, Data, Ora, CodAlimento, Quantita
                FROM CONSUMAZIONE
                WHERE Username = ?
                ORDER BY Data DESC, Ora DESC
                """;
        try (PreparedStatement s = connection.prepareStatement(LEGGI_CONSUMAZIONI)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Consumazione> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Consumazione(
                        res.getString(1),
                        res.getInt(2),
                        res.getDate(3).toLocalDate(),
                        res.getTime(4).toLocalTime(),
                        res.getInt(5),
                        res.getInt(6)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungiConsumazione(Consumazione consumazione) {
        final String AGGIUNGI_CONSUMAZIONE = """
                CALL inserisci_consumazione(?,?,?,?,?);
                """;
        try (PreparedStatement s = connection.prepareStatement(AGGIUNGI_CONSUMAZIONE)) {
            s.setString(1, consumazione.username());
            s.setDate(2, Date.valueOf(consumazione.data()));
            s.setTime(3, Time.valueOf(consumazione.ora()));
            s.setInt(4, consumazione.codAlimento());
            s.setInt(5, consumazione.quantità());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean modificaConsumazione(Consumazione nuoviValori) {
        final String AGGIUNGI_CONSUMAZIONE = """
                UPDATE CONSUMAZIONE
                SET Data = ?, Ora = ?, CodAlimento = ?, Quantita = ?
                WHERE Username = ? AND Numero = ?
                        """;
        try (PreparedStatement s = connection.prepareStatement(AGGIUNGI_CONSUMAZIONE)) {
            s.setDate(1, Date.valueOf(nuoviValori.data()));
            s.setTime(2, Time.valueOf(nuoviValori.ora()));
            s.setInt(3, nuoviValori.codAlimento());
            s.setInt(4, nuoviValori.quantità());
            s.setString(5, nuoviValori.username());
            s.setInt(6, nuoviValori.numero());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean rimuoviConsumazione(Consumazione consumazione) {
        final String RIMUOVI_CONSUMAZIONE = """
                DELETE FROM CONSUMAZIONE
                WHERE Username = ?
                AND Numero = ?
                """;
        try (PreparedStatement s = connection.prepareStatement(RIMUOVI_CONSUMAZIONE)) {
            s.setString(1, consumazione.username());
            s.setInt(2, consumazione.numero());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean aggiungiRicetta(Ricetta ricetta) {
        final String INSERISCI_VALORI_TEMPORANEI = """
                INSERT INTO ALIMENTO (Nome, Kcal, Carboidrati, Grassi, Proteine, Porzione, Tipo, Proprietario, Privato)
                VALUES (?, 1, 1, 1, 1, ?, 'R', ?, ?)
                """;
        final String INSERISCI_COMPOSIZIONE = """
                INSERT INTO COMPOSIZIONE
                VALUES (?,?,?)
                """;
        final String AGGIORNA_RICETTA = """
                CALL aggiorna_ricetta(?)
                """;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement inserisciRicetta = connection.prepareStatement(INSERISCI_VALORI_TEMPORANEI,
                    Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement inserisciComponente = connection.prepareStatement(INSERISCI_COMPOSIZIONE);
                    PreparedStatement aggiornaRicetta = connection.prepareCall(AGGIORNA_RICETTA)) {

                inserisciRicetta.setString(1, ricetta.ricetta().nome());
                inserisciRicetta.setObject(2, ricetta.ricetta().porzione().orElse(null));
                inserisciRicetta.setObject(3, ricetta.username());
                inserisciRicetta.setObject(4, ricetta.ricetta().privata());
                if (inserisciRicetta.executeUpdate() != 1) {
                    throw new SQLException("Errore durante l'aggiunta dei valori temporanei della ricetta.");
                }

                ResultSet generatedKey = inserisciRicetta.getGeneratedKeys();
                generatedKey.next();
                int codRicetta = generatedKey.getInt(1);

                for (var ingrediente : ricetta.ingredienti().entrySet()) {
                    inserisciComponente.setInt(1, codRicetta);
                    inserisciComponente.setInt(2, ingrediente.getKey().codAlimento());
                    inserisciComponente.setInt(3, ingrediente.getValue());
                    inserisciComponente.addBatch();
                }

                if (ricetta.ingredienti().size() > 0
                        && Arrays.stream(inserisciComponente.executeBatch()).anyMatch(i -> i != 1)) {
                    throw new SQLException("Errore durante l'aggiunta di un ingrediente alla ricetta.");
                }

                aggiornaRicetta.setInt(1, codRicetta);
                if (aggiornaRicetta.executeUpdate() != 1) {
                    throw new SQLException("Errore durante l'aggiornamento dei valori della ricetta.");
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                System.err.println(e);
                connection.rollback();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean modificaValoriCibo(Alimento cibo, ValoriCibo nuoviValori) {
        final String MODIFICA_CIBO = """
                UPDATE ALIMENTO
                SET Nome = ?, Kcal = ?, Proteine = ?, Grassi = ?, Carboidrati = ?, Privato = ?
                WHERE CodAlimento = ?
                """;
        final String AGGIORNA_RICETTE = """
                CALL aggiorna_ricette(?)
                """;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement modifica = connection.prepareStatement(MODIFICA_CIBO);
                    PreparedStatement aggiorna = connection.prepareStatement(AGGIORNA_RICETTE)) {
                modifica.setString(1, nuoviValori.nome());
                modifica.setInt(2, nuoviValori.kcal());
                modifica.setInt(3, nuoviValori.proteine());
                modifica.setInt(4, nuoviValori.grassi());
                modifica.setInt(5, nuoviValori.carboidrati());
                modifica.setBoolean(6, nuoviValori.privato());
                modifica.setInt(7, cibo.codAlimento());
                if (modifica.executeUpdate() > 0) {
                    aggiorna.setInt(1, cibo.codAlimento());
                    aggiorna.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                System.err.println(e);
                connection.rollback();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean modificaValoriRicetta(Alimento ricetta, ValoriRicetta nuoviValori) {
        final String MODIFICA_VALORI = """
                UPDATE ALIMENTO
                SET Nome = ?, Porzione = ?, Privato = ?
                WHERE CodAlimento = ?
                """;
        try (PreparedStatement s = connection.prepareStatement(MODIFICA_VALORI)) {
            s.setString(1, ricetta.nome());
            s.setObject(2, ricetta.porzione().orElse(null));
            s.setBoolean(3, ricetta.privato());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean eliminaAlimento(Alimento alimento) {
        String ELIMINA_ALIMENTO = "DELETE FROM ALIMENTO WHERE CodAlimento = ?";
        try (PreparedStatement s = connection.prepareStatement(ELIMINA_ALIMENTO)) {
            s.setInt(1, alimento.codAlimento());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public Map<Alimento, Integer> leggiIngredienti(Alimento ricetta) {
        final String LEGGI_INGREDIENTI = """
                SELECT Ingrediente, Nome, Kcal, Carboidrati, Grassi, Proteine, Porzione, Tipo, Brand, Proprietario, Privato, Quantita
                FROM Composizione c JOIN Alimento a ON c.Ingrediente = a.CodAlimento
                WHERE c.Ricetta = ?
                """;
        try (PreparedStatement s = connection.prepareStatement(LEGGI_INGREDIENTI)) {
            s.setInt(1, ricetta.codAlimento());
            Map<Alimento, Integer> ret = new HashMap<>();
            ResultSet res = s.executeQuery();
            while (res.next()) {
                ret.put(new Alimento(res.getInt(1),
                        res.getString(2),
                        res.getInt(3),
                        res.getInt(4),
                        res.getInt(5),
                        res.getInt(6),
                        Optional.ofNullable(res.getObject(7, Integer.class)),
                        res.getString(8).charAt(0),
                        Optional.ofNullable(res.getObject(9, String.class)),
                        res.getString(10),
                        res.getBoolean(11)),
                        res.getInt(12));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean modificaIngredienti(Alimento ricetta, Map<Alimento, Integer> attuali, Map<Alimento, Integer> nuovi) {
        final String INSERISCI_COMPOSIZIONE = """
                INSERT INTO COMPOSIZIONE
                VALUES (?,?,?)
                """;
        final String RIMUOVI_COMPOSIZIONE = """
                DELETE FROM COMPOSIZIONE
                WHERE Ricetta = ? AND Ingrediente = ?
                """;
        final String MODIFICA_COMPOSIZIONE = """
                UPDATE COMPOSIZIONE
                SET Quantita = ?
                WHERE Ricetta = ? AND Ingrediente = ?
                """;
        final String AGGIORNA_RICETTA = """
                CALL aggiorna_ricetta(?)
                """;

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement inserisciComponente = connection.prepareStatement(INSERISCI_COMPOSIZIONE);
                    PreparedStatement rimuoviComponente = connection.prepareStatement(RIMUOVI_COMPOSIZIONE);
                    PreparedStatement modificaComponente = connection.prepareStatement(MODIFICA_COMPOSIZIONE);
                    PreparedStatement aggiornaRicetta = connection.prepareCall(AGGIORNA_RICETTA)) {

                var daAggiungere = new HashSet<>(nuovi.keySet());
                daAggiungere.removeAll(attuali.keySet());
                for (var ingrediente : daAggiungere) {
                    System.out.println("Aggiungo " + nuovi.get(ingrediente) + "grammi di\n" + ingrediente);
                    inserisciComponente.setInt(1, ricetta.codAlimento());
                    inserisciComponente.setInt(2, ingrediente.codAlimento());
                    inserisciComponente.setInt(3, nuovi.get(ingrediente));
                    inserisciComponente.addBatch();
                }
                if (daAggiungere.size() > 0
                        && Arrays.stream(inserisciComponente.executeBatch()).anyMatch(i -> i != 1)) {
                    throw new SQLException("Errore durante l'aggiunta di un ingrediente alla ricetta.");
                }

                var daModificare = new HashSet<>(nuovi.keySet());
                daModificare.retainAll(attuali.keySet());
                for (var ingrediente : daModificare) {
                    System.out.println("Imposto " + nuovi.get(ingrediente) + "grammi di\n" + ingrediente);
                    modificaComponente.setInt(1, nuovi.get(ingrediente));
                    modificaComponente.setInt(2, ricetta.codAlimento());
                    modificaComponente.setInt(3, ingrediente.codAlimento());
                    modificaComponente.addBatch();
                }
                if (daModificare.size() > 0 && Arrays.stream(modificaComponente.executeBatch()).anyMatch(i -> i != 1)) {
                    throw new SQLException("Errore durante la modifica della quantità di un ingrediente alla ricetta.");
                }

                var daEliminare = new HashSet<>(attuali.keySet());
                daEliminare.removeAll(nuovi.keySet());
                for (var ingrediente : daEliminare) {
                    System.out.println("Rimuovo\n" + ingrediente);
                    rimuoviComponente.setInt(1, ricetta.codAlimento());
                    rimuoviComponente.setInt(2, ingrediente.codAlimento());
                    rimuoviComponente.addBatch();
                }
                if (daEliminare.size() > 0 && Arrays.stream(rimuoviComponente.executeBatch()).anyMatch(i -> i != 1)) {
                    throw new SQLException("Errore durante la rimozione di un ingrediente dalla ricetta.");
                }

                aggiornaRicetta.setInt(1, ricetta.codAlimento());
                aggiornaRicetta.executeQuery();
                connection.commit();
                return true;
            } catch (SQLException e) {
                System.err.println(e);
                connection.rollback();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Valori calcolaValoriGiorno(String username, LocalDate data) {
        final String CALCOLA_VALORI = """
                SELECT SUM(a.Kcal * c.Quantita / 100) AS Calorie,
                SUM(a.Proteine * c.Quantita / 100) AS Proteine,
                SUM(a.Grassi * c.Quantita / 100) AS Grassi,
                SUM(a.Carboidrati * c.Quantita / 100) AS Carboidrati
                FROM CONSUMAZIONE c
                JOIN ALIMENTO a ON c.CodAlimento = a.CodAlimento
                WHERE c.Username = ? AND c.Data = ?;
                """;
        try (PreparedStatement s = connection.prepareStatement(CALCOLA_VALORI)) {
            s.setString(1, username);
            s.setDate(2, Date.valueOf(data));
            var res = s.executeQuery();
            res.next();
            return new Valori(res.getInt(1), res.getInt(2), res.getInt(3), res.getInt(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Valori calcolaValoriMediPeriodo(String username, LocalDate dataInizio, LocalDate dataFine) {
        final String CALCOLA_VALORI = """
                CALL CalcolaValoriNutrizionaliMedi(?,?,?)
                """;
        try (PreparedStatement s = connection.prepareStatement(CALCOLA_VALORI)) {
            s.setString(1, username);
            s.setDate(2, Date.valueOf(dataInizio));
            s.setDate(3, Date.valueOf(dataFine));
            var res = s.executeQuery();
            res.next();
            return new Valori(res.getInt(1), res.getInt(2), res.getInt(3), res.getInt(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<BigDecimal> differenzaDiPeso(String username, LocalDate dataInizio, LocalDate dataFine) {
        final String CALCOLA_DIFFERENZA = """
                WITH PesoIniziale AS (
                    SELECT Peso
                    FROM MISURAZIONE
                    WHERE Username = ? AND Data <= ?
                    ORDER BY Data ASC
                    LIMIT 1
                ), PesoFinale AS (
                    SELECT Peso
                    FROM MISURAZIONE
                    WHERE Username = ? AND Data <= ?
                    ORDER BY Data DESC
                    LIMIT 1
                )
                SELECT (PesoFinale.Peso - PesoIniziale.Peso) AS Differenza
                FROM PesoIniziale, PesoFinale
                """;
        try (PreparedStatement s = connection.prepareStatement(CALCOLA_DIFFERENZA)) {
            s.setString(1, username);
            s.setDate(2, Date.valueOf(dataInizio));
            s.setString(3, username);
            s.setDate(4, Date.valueOf(dataFine));
            var res = s.executeQuery();

            return Optional.ofNullable(res.next() ? res.getObject(1, BigDecimal.class) : null);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<BigDecimal> stimaTdee(String username, LocalDate dataInizio, LocalDate dataFine) {
        final String CALCOLA_TDEE = """
                CALL CalcolaStimaTDEE(?,?,?)
                """;
        try (PreparedStatement s = connection.prepareStatement(CALCOLA_TDEE)) {
            s.setString(1, username);
            s.setDate(2, Date.valueOf(dataInizio));
            s.setDate(3, Date.valueOf(dataFine));
            var res = s.executeQuery();

            return Optional.ofNullable(res.next() ? res.getObject(1, BigDecimal.class) : null);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
