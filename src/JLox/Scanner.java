package JLox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    private final String source;

    // Tokenized source
    private final List<Token> tokens = new ArrayList<>();

    // Working position
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Keywords
    private static final Map<String, TokenType> keywords;

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
                } else if (match('*')) {
                    // Ignore until multiline comments reach depth 0
                    multilineComment();
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

            // String
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    JLox.error(line, "Unexpected character.");
                }
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

    private void multilineComment() { //TODO newline char and switch statement
        int depth = 1;
        while (depth > 0) {
            while ((peek() != '/' && peek() != '*') && !isAtEnd()) advance();
            if (isAtEnd()) break;
            if (peek() == '/' && peekNext() == '*') {
                depth++;
                advance();
            }
            if (peek() == '*' && peekNext() == '/') {
                depth--;
                advance();
            }
            advance();
        }
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

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
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

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }
}
