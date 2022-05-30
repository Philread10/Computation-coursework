import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;
import computation.derivation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Parser implements IParser {

  //This is the method for Part C
  public boolean isInLanguage(ContextFreeGrammar cfg, Word w){

    //This block gets the list of rules from the given cfg. It also gets the start variable for the given CFG, and creates a new derivation with that start variable. It adds the new derivation to a new list of derivations.
    Word startWord = new Word(cfg.getStartVariable());
    List<Rule> rules = cfg.getRules(); 
    List<Derivation> activeDerivations = new ArrayList<Derivation>(); 
    Derivation Current_Step_Derivation = new Derivation(startWord);
    activeDerivations.add(Current_Step_Derivation); 

    //Max number of iterations is the length (n) of the input word (w) * 2 -1. This block sets the variables to count the steps, and defines n based on the input word. It also creates the variable index for later. 
    int steps = 0; 
    int n = w.length();
    int index;

    //This is the main while loop. The logic is that it iterates through every derivation in the list created above (starting with just a start symbol), and then tries to apply each rule to the latest word in the derivation (this will be the word that is "closest" to our target). NumVariables and Index are used to assign the rule to a single symbol in the word.
    
    while(steps < (2*n - 1)) {
      List<Derivation> newActiveDerivations = new ArrayList<Derivation>();  
      for(Derivation currentDerivation : activeDerivations){
        Word currentWord = currentDerivation.getLatestWord();
        for (Rule currentRule : rules) {
          int NumVariables = currentWord.count(currentRule.getVariable());
          for (int i = 0; i < NumVariables; i++) {
            index = currentWord.indexOfNth(currentRule.getVariable(), i);
            Word applyRule = currentWord.replace(index, currentRule.getExpansion());

            //This block creates a new derivation using the derivation currently being assessed in the first for loop. It adds the next "step" to it, and adds this to our newActiveDerivations list. 
						Derivation newDev = new Derivation(currentDerivation);
						newDev.addStep(applyRule, currentRule, index);
						newActiveDerivations.add(newDev);
          }
        }
      }

      //Once all derivations have their next step appended, this overwrites the old list. The steps go up, and a new loop repeats assessing the latest word in each derivation.
      activeDerivations = newActiveDerivations;
      steps++;

      //Finally, this block looks at the last word in every derivation in our last, and compares that to the target word.
      for(Derivation alldevs : activeDerivations){
        Word testWords = alldevs.getLatestWord();
        String targetWord = w.toString();
        String finalWords = testWords.toString();
        if (finalWords.equals(targetWord)){
          return true;
        }     
      }
    }
    return false; 
  }


  //This is the Method for Part D
  public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {

    //The first block of this code is a duplicate of Part C. I have marked where the part D code "begins" further down.
    Word startWord = new Word(cfg.getStartVariable());
    List<Rule> rules = cfg.getRules(); 
    List<Derivation> activeDerivations = new ArrayList<Derivation>(); 
    Derivation Current_Step_Derivation = new Derivation(startWord);
    activeDerivations.add(Current_Step_Derivation);
    List<ParseTreeNode> tempChildren = new ArrayList<ParseTreeNode>();
    List<ParseTreeNode> finalNode = new ArrayList<ParseTreeNode>();

    int steps = 0; 
    int n = w.length();
    int index;

    while(steps < (2*n - 1)) {
      List<Derivation> newActiveDerivations = new ArrayList<Derivation>();  
      for(Derivation currentDerivation : activeDerivations){
        Word currentWord = currentDerivation.getLatestWord();
        for (Rule currentRule : rules) {
          int NumVariables = currentWord.count(currentRule.getVariable());
          for (int i = 0; i < NumVariables; i++) {
            index = currentWord.indexOfNth(currentRule.getVariable(), i);
            Word applyRule = currentWord.replace(index, currentRule.getExpansion());
						Derivation newDev = new Derivation(currentDerivation);
						newDev.addStep(applyRule, currentRule, index);
						newActiveDerivations.add(newDev);
          }
        }
      }            

      activeDerivations = newActiveDerivations;
      steps++;

      for(Derivation alldevs : activeDerivations){
        Word testWords = alldevs.getLatestWord();
        String targetWord = w.toString();
        String finalWords = testWords.toString();

        //This is where we implement the pass tree, and part D really begins.

        //If the word is in the language, a Node list is created
        if (finalWords.equals(targetWord)){

          List<ParseTreeNode> parseNodes = new ArrayList<ParseTreeNode>();

          //We enter a for loop for the "successful" derivation. We retrieve the relevant rule for each step. If the expansion of the rule has 1 Symbol, that becomes a node and the left hand side becomes a second node, with the first node as a child. Both are added to the list above. If the expansion has 2 Symbols, we check whether each symbol already corresponds to a node. If it does, the left hand side of the rule becomes a new node, with the corresponding node as a child. These are also added to the list.
          for(Step s : alldevs){
            try {
              Rule nodeRule = s.getRule();
              Symbol leftSide = nodeRule.getVariable();
              int count = nodeRule.getExpansion().length();
              //where expansion has 1 symbol (terminal)
              if(count == 1){
                for(Symbol symbols : nodeRule.getExpansion()){
                  ParseTreeNode newNode = new ParseTreeNode(symbols);
                  parseNodes.add(newNode);
                  ParseTreeNode newNewNode = new ParseTreeNode(leftSide, newNode);
                  parseNodes.add(newNewNode);
                }
              }
              //Where expansion has 2 symbols
              else{
                for (Symbol symbols : nodeRule.getExpansion()) {
                  for (ParseTreeNode existingNodes: parseNodes){
                    if(existingNodes.getSymbol().equals(symbols)){
                      if(tempChildren.contains(existingNodes)){
                        //pass
                      }
                      else{
                        tempChildren.add(existingNodes);
                      }
                      break;
                    }
                  }
                }

                if(tempChildren.size() > 1){
                  ParseTreeNode terminalNode = new ParseTreeNode(leftSide, tempChildren.get(0), tempChildren.get(1));
                  parseNodes.add(terminalNode);
                  tempChildren.clear();
                }
                else{
                  ParseTreeNode terminalNode = new ParseTreeNode(leftSide, tempChildren.get(0));
                  parseNodes.add(terminalNode);
                  tempChildren.clear();                  
                }                  
              }
            }
            //Catches the error where we try to read the null rule at the end. At this point, the tree should be complete.
            catch(Exception e){
              break;
            }          
          } 
          //We add the final node to the "final node" list
          finalNode.add(parseNodes.get((parseNodes.size()-1)));
          break;
        }     
      }      
    }
    //Return the final node.   
    return finalNode.get(0);
  }    
}
