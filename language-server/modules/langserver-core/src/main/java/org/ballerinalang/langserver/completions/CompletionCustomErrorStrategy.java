/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Capture possible errors from source.
 */
public class CompletionCustomErrorStrategy extends LSCustomErrorStrategy {

    private LSContext context;

    private boolean overriddenTokenIndex = false;
    private boolean overriddenContext = false;

    public CompletionCustomErrorStrategy(LSContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        fillContext(parser, e.getOffendingToken());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        fillContext(parser, parser.getCurrentToken());
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        fillContext(parser, e.getOffendingToken());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        fillContext(parser, parser.getCurrentToken());
    }

    private void fillContext(Parser parser, Token currentToken) {
        // If the token index is overridden then we skip rest of the errors
        if (overriddenTokenIndex) {
            return;
        }
        ParserRuleContext currentContext = parser.getContext();
        /*
        TODO: Specific check  is added in order to handle the completion inside an 
        endpoint context. This particular case need to remove after introducing a proper handling mechanism or with
         the introduction of BNF grammar. Also check the 
         */
        boolean isWithinEndpointContext = this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY) == null ? false :
                this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                        .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY);
        if (isCursorBetweenGivenTokenAndLastNonHiddenToken(currentToken, parser)
                || (!isWithinEndpointContext && this.isWithinEndpointContext(parser))) {
            this.context.put(DocumentServiceKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
            this.context.put(DocumentServiceKeys.VOCABULARY_KEY, parser.getVocabulary());
            if (!overriddenTokenIndex) {
                this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, parser.getCurrentToken().getTokenIndex());
            }
            if (!overriddenContext) {
                this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, currentContext);
            }
        }
    }

    /**
     * Checks whether cursor is within the whitespace region between current token to last token.
     *
     * @param token  Token to be evaluated
     * @param parser Parser Instance
     * @return true|false
     */
    private boolean isCursorBetweenGivenTokenAndLastNonHiddenToken(Token token, Parser parser) {
        this.setContextException(parser);
        boolean isCursorBetween = false;
        int line = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int character = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();

        Token lastNonHiddenToken = null;
        for (int tokenIdx = token.getTokenIndex() - 1; tokenIdx >= 0; tokenIdx--) {
            Token lastToken = parser.getTokenStream().get(tokenIdx);
            if (lastToken.getChannel() != Token.HIDDEN_CHANNEL) {
                lastNonHiddenToken = lastToken;
                break;
            }
        }
        if (lastNonHiddenToken != null) {
            if (this.overrideTokenIndex(parser, lastNonHiddenToken)) {
                return true;
            }

            // Convert the token lines and char positions to zero based indexing
            int lastNonHiddenTokenLine = lastNonHiddenToken.getLine() - 1;
            int lastNonHiddenTokenChar = lastNonHiddenToken.getCharPositionInLine();
            int tokenLine = token.getLine() - 1;
            int tokenChar = token.getCharPositionInLine();

            if (line >= lastNonHiddenTokenLine && line <= tokenLine) {
                if (line == lastNonHiddenTokenLine) {
                    isCursorBetween = character >= (lastNonHiddenTokenChar + lastNonHiddenToken.getText().length());
                } else {
                    isCursorBetween = line != tokenLine || character <= tokenChar;
                }
            }
        }
        return isCursorBetween;
    }

    private boolean isWithinEndpointContext(Parser parser) {
        int currentTokenIndex = parser.getCurrentToken().getTokenIndex();
        TokenStream tokenStream = parser.getTokenStream();
        List<String> terminalTokens = Arrays.asList("{", ";", "}", "(", ")");
        String tokenString = tokenStream.get(currentTokenIndex).getText();
        boolean isWithinEndpoint = false;
        
        if (tokenString.equals(UtilSymbolKeys.OPEN_BRACE_KEY)) {
            currentTokenIndex -= 1;
            boolean cursorAfterEndpointKeyword = false;
            String endpointName = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex).getText();

            // Set the endpoint name to the meta info context
            this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                    .put(CompletionKeys.META_CONTEXT_ENDPOINT_NAME_KEY, endpointName);

            while (true) {
                if (currentTokenIndex < 0) {
                    // If this stage occurred, then the current context is not able to resolve the endpoint start
                    return false;
                }
                Token previousDefaultToken = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
                tokenString = previousDefaultToken.getText();
                if (terminalTokens.contains(tokenString)) {
                    break;
                } else if (tokenString.equals(UtilSymbolKeys.ENDPOINT_KEYWORD_KEY)) {
                    cursorAfterEndpointKeyword = true;
                    currentTokenIndex = parser.getCurrentToken().getTokenIndex();
                    break;
                }

                currentTokenIndex = previousDefaultToken.getTokenIndex();
            }

            if (cursorAfterEndpointKeyword) {
                while (true) {
                    Token nextDefaultToken = CommonUtil.getNextDefaultToken(tokenStream, currentTokenIndex);
                    if (nextDefaultToken == null) {
                        return false;
                    }
                    tokenString = nextDefaultToken.getText();
                    
                    if (tokenString.equals(UtilSymbolKeys.CLOSE_BRACE_KEY)) {
                        int cursorLine = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
                        int cursorCol = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
                        int startTokenLine = tokenStream.get(parser.getCurrentToken().getTokenIndex()).getLine() - 1;
                        int startTokenCol = tokenStream
                                .get(parser.getCurrentToken().getTokenIndex()).getCharPositionInLine();
                        int endTokenLine = nextDefaultToken.getLine() - 1;
                        int endTokenCol = nextDefaultToken.getCharPositionInLine();
                        
                        isWithinEndpoint = (cursorLine > startTokenLine && cursorLine < endTokenLine)
                                || (cursorLine == startTokenLine && cursorCol >= startTokenCol
                                && cursorLine < endTokenLine)
                                || (cursorLine > startTokenLine && cursorLine == endTokenLine
                                && cursorCol <= endTokenCol)
                                || (cursorLine == startTokenLine && cursorLine == endTokenLine
                                && cursorCol > startTokenCol && cursorCol < endTokenCol);
                        break;
                    } else if (terminalTokens.contains(tokenString)) {
                        break;
                    }

                    currentTokenIndex = nextDefaultToken.getTokenIndex();
                }
            }
        }

        this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                .put(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY, isWithinEndpoint);
        return isWithinEndpoint;
    }
    
    private boolean overrideTokenIndex(Parser parser, Token lastNonHiddenToken) {
        Token tokenBefore = CommonUtil.getPreviousDefaultToken(parser.getTokenStream(),
                lastNonHiddenToken.getTokenIndex());
        if (tokenBefore != null && ((UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY.equals(tokenBefore.getText())
                && parser.getContext() instanceof BallerinaParser.ServiceBodyContext)
                || (UtilSymbolKeys.IMPORT_KEYWORD_KEY.equals(tokenBefore.getText())
                && parser.getContext() instanceof BallerinaParser.ImportDeclarationContext)
                || (UtilSymbolKeys.MATCH_KEYWORD_KEY.equals(tokenBefore.getText())
                && parser.getContext() instanceof BallerinaParser.MatchStatementContext))) {
            overriddenTokenIndex = true;
            this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, tokenBefore.getTokenIndex());
            return true;
        } else if ((parser.getContext() instanceof BallerinaParser.SimpleVariableReferenceContext
                && this.isServiceEndpointBindContext(parser))
                || isEndpointTypeContext(parser)) {
            overriddenTokenIndex = true;
            return true;
        }
        
        return false;
    }

    private boolean isServiceEndpointBindContext(Parser parser) {
        // this iteration number is added in order to avoid unnecessary traverses. This specific logic need to revisit
        // after proper grammar fixes (BNF)
        int iterations = 6;
        ParserRuleContext currentContext = parser.getContext();
        ParserRuleContext contextParent = currentContext.getParent();
        List<ParserRuleContext> contextList = new ArrayList<>();

        while (contextParent != null && iterations > 0) {
            if (contextParent instanceof BallerinaParser.ServiceEndpointAttachmentsContext) {
                // Move this properly to the set context Exception
                InputMismatchException inputMismatchException = new InputMismatchException(parser);
                contextParent.exception = inputMismatchException;
                this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, contextParent.start.getTokenIndex());
                this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, contextParent);
                this.overriddenContext = true;

                contextList.forEach(parserRuleContext -> parserRuleContext.exception = inputMismatchException);

                return true;
            }

            contextList.add(contextParent);
            contextParent = contextParent.getParent();
            iterations--;
        }

        return false;
    }
    
    private boolean isEndpointTypeContext(Parser parser) {
        if ((parser.getContext() instanceof BallerinaParser.NameReferenceContext
                && parser.getContext().getParent() instanceof BallerinaParser.EndpointTypeContext)
                || parser.getContext() instanceof BallerinaParser.EndpointDeclarationContext) {
            // Move this properly to the set context Exception
            InputMismatchException inputMismatchException = new InputMismatchException(parser);
            ParserRuleContext context = parser.getContext();
            context.exception = inputMismatchException;
            context.getParent().exception = inputMismatchException;
            context.getParent().getParent().exception = inputMismatchException;
            this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, context.getParent().start.getTokenIndex());
            this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, context.getParent());
            this.overriddenContext = true;

            return true;
        }
        
        return false;
    }

    @Override
    public void reportMatch(Parser recognizer) {
        super.reportMatch(recognizer);
        if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && isInLastTermination(recognizer)) {
            // -2 since Parser.match() consumes one extra + skip current token
            deleteTokensUpToCursor(recognizer, -2);
        }
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException {
        if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && isInFirstTokenOfCursorLine(recognizer)) {
            // -1 since skip current token
            deleteTokensUpToCursor(recognizer, -1);
        }
        super.sync(recognizer);
    }

    private boolean isInFirstTokenOfCursorLine(Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        Token firstTokenOfCursorLine = getFirstTokenOfCursorLine(recognizer.getInputStream());
        return firstTokenOfCursorLine != null && currentToken.getTokenIndex() == firstTokenOfCursorLine.getTokenIndex();
    }

    private boolean isInLastTermination(Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        Token lastTerminationToken = getLastTerminationToken(recognizer.getInputStream());
        return lastTerminationToken != null && currentToken.getTokenIndex() == lastTerminationToken.getTokenIndex();
    }

    private void deleteTokensUpToCursor(Parser recognizer, int tokenOffset) {
        Position cursorPosition = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = cursorPosition.getLine() + 1;
        int cursorCol = cursorPosition.getCharacter() + 1;

        int index = 1;
        Token beforeCursorToken = recognizer.getInputStream().LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();
        int tCol = beforeCursorToken.getCharPositionInLine();

        int needToRemoveTokenCount = tokenOffset;
        while (type != BallerinaParser.EOF && ((tLine < cursorLine) || (tLine == cursorLine && tCol < cursorCol))) {
            beforeCursorToken = recognizer.getInputStream().LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
            tCol = beforeCursorToken.getCharPositionInLine();
            index++;
            needToRemoveTokenCount++;
        }

        while (needToRemoveTokenCount > 0){
            recognizer.consume();
            needToRemoveTokenCount--;
        }

        Token currentToken = recognizer.getCurrentToken();
        Token prevToken = recognizer.getInputStream().LT(-1);
        if (currentToken.getLine() == prevToken.getLine()) {
            //To avoid catching incorrect contexts
            ParserRuleContext context = recognizer.getContext();
            this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, currentToken.getTokenIndex());
            this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, context);
            this.context.put(DocumentServiceKeys.TOKEN_STREAM_KEY, recognizer.getTokenStream());
        }
    }

    private Token getFirstTokenOfCursorLine(TokenStream tokenStream){
        Token firstCursorLineToken = null;
        int cursorLine = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine() + 1;

        int index = 1;
        Token beforeCursorToken = tokenStream.LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();

        firstCursorLineToken = (tLine == cursorLine) ? beforeCursorToken : null;

        while (type != BallerinaParser.EOF && (tLine <= cursorLine)) {
            if (tLine == cursorLine) {
                firstCursorLineToken = beforeCursorToken;
            }
            index++;
            beforeCursorToken = tokenStream.LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
        }
        return firstCursorLineToken;
    }

    private Token getLastTerminationToken(TokenStream tokenStream) {
        Token lastTerminationToken = null;
        Position cursorPosition = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = cursorPosition.getLine() + 1;
        int cursorCol = cursorPosition.getCharacter() + 1;

        int index = 1;
        Token beforeCursorToken = tokenStream.LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();
        int tCol = beforeCursorToken.getCharPositionInLine();

        while (type != BallerinaParser.EOF
                && ((tLine < cursorLine) || (tLine == cursorLine && tCol < cursorCol))) {
            beforeCursorToken = tokenStream.LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
            tCol = beforeCursorToken.getCharPositionInLine();
            if (beforeCursorToken.getTokenIndex() == 1 || type == BallerinaParser.SEMICOLON ||
                    type == BallerinaParser.LEFT_BRACE || type == BallerinaParser.RIGHT_BRACE ||
                    type == BallerinaParser.COMMA) {
                lastTerminationToken = beforeCursorToken;
            }
            index++;
        }
        return lastTerminationToken;
    }
}
