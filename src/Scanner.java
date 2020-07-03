import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final String source;

    // Tokenized source
    private final List<Token> tokens = new ArrayList<>();

    // Working position
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    /**
     * Scans through code and produces tokens
     * @return
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * categorises individual tokens
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single chars
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            // Single or double chars
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '/':
                if (match('/')) {
                    // Ignore until end of line is reached
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Ignore white spaces
            case ' ':
            case '\r':
            case '\t':
                break;

            // Newline
            case '\n': line++; break;

            case '"': string(); break;

            default:
                JLox.error(line, "unexpected character.");
                break;
        }
    }

    /**
     * Determines if next char matches for two character tokens
     * @param expected
     * @return true if two character token
     */
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // crates Lox syntax tokens
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text =  source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Unterminated string
        if (isAtEnd()) {
            JLox.error(line, "Unterminated string"); //TODO line should be at start of string?
        }

        // " has been found and should be consumed.
        advance();

        // remove "'s from string
        addToken(TokenType.STRING, source.substring(start + 1, current - 1));
    }

    private boolean isAtEnd() {
        return  current >= source.length();
    }

    // returns next character and consumes current
    private char advance() {
        return source.charAt(current++);
    }

    // returns next character without consuming current
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
}
