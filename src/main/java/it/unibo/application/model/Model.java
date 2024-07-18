package it.unibo.application.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import it.unibo.application.commons.Utilities;
import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Consumazione;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Ricetta;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;
import it.unibo.application.dto.ValoriCibo;

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

    public Optional<Integer> leggiObbiettivo(String username) {
        final String LEGGI_OBBIETTIVO = "SELECT Obbiettivo FROM UTENTE WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_OBBIETTIVO)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            if (!res.next()) {
                throw new IllegalArgumentException("No such user: \"" + username + "\"");
            } else {
                int val = res.getInt(1);
                return Optional.ofNullable(val > 0 ? val : null);
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
                int kcal = res.getInt(1);
                return Optional.ofNullable(kcal > 0 ? new Target(
                        kcal,
                        Utilities.strictlyPositive(res.getInt(2)),
                        Utilities.strictlyPositive(res.getInt(3)),
                        Utilities.strictlyPositive(res.getInt(4)))
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

    public boolean impostaObbiettivo(String username, Optional<Integer> obbiettivo) {
        final String IMPOSTA_TARGET = "UPDATE UTENTE SET Obbiettivo = ? WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(IMPOSTA_TARGET)) {
            if (obbiettivo.isPresent()) {
                s.setInt(1, obbiettivo.get());
            } else {
                s.setNull(1, Types.DECIMAL);
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

    public List<Tag> leggiTag(String username) {
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

    public boolean aggiungiAlimento(Alimento cibo) {
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
            s.setInt(10, cibo.privato() ? 1 : 0);
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
        System.out.println(nuoviValori);
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
        return false;
    }

    public boolean modificaCibo(Alimento cibo, ValoriCibo nuoviValori) {
        return false;
    }

    public boolean eliminaAlimento(Alimento alimento) {
        return false;
    }
}
