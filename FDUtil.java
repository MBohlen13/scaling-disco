import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

/**
 * This utility class is not meant to be instantitated, and just provides some
 * useful methods on FD sets.
 * 
 * @author Moxie Bohlen
 * @version 10/21/2026
 */
public final class FDUtil {
  /**
   * Generates all trivial FDs over the given attribute set.
   * FDs with empty right-hand sides are omitted.
   *
   * @param attrs the relation's attributes; must not be modified
   * @return a new set containing all trivial FDs over the given attributes
   */
  public static FDSet trivial(final Set<String> attrs) {
    Set<Set<String>> power = powerSet(attrs);
    Object[] mySets = power.toArray();

    FDSet triv = new FDSet();
    for(int i = 0; i < power.size(); i++){
      Set<String> curr = (Set<String>) mySets[i];
      if(!curr.isEmpty()){
        for(int j = 0; j < power.size(); j++){
            Set<String> comp = (Set<String>) mySets[j];

            if(curr.containsAll(comp)&&!comp.isEmpty()){
                FD temp = new FD(curr, comp);
                triv.add(temp);
            }
        }
      }
    }
    return triv;
  }

  /**
   * Augments every FD in the given set of FDs with the given attributes
   * 
   * @param fdset FD Set (Immutable)
   * @param attrs a set of attributes with which to augment FDs (Immutable)
   * @return a set of augmented FDs
   */
  public static FDSet augment(final FDSet fdset, final Set<String> attrs) {
    Set<FD> mySets = fdset.getSet();
    FDSet augments = new FDSet();

    for(FD sets : mySets){
        Set<String> left = new TreeSet<>(sets.getLeft());
        Set<String> right = new TreeSet<>(sets.getRight());
        left.addAll(attrs);
        right.addAll(attrs);
        FD temp = new FD(left, right);
        augments.add(temp);
    }

    return augments;
  }

  /**
   * Exhaustively resolves transitive FDs with respect to the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitive(final FDSet fdset) {
    TreeSet<FD> mySets = (TreeSet<FD>) fdset.getSet();
    FDSet transitives = new FDSet();
    for(FD sets : mySets){
      for(FD otherSets : mySets){
        if(sets.getRight().containsAll(otherSets.getLeft())){
          FD temp = new FD(sets.getLeft(), otherSets.getRight());
          if(!mySets.contains(temp)){
          transitives.add(temp);
          }
        }
      }
    }

    while(true){
      FDSet check = new FDSet(transitives);
      for(FD sets : mySets){
        for(FD otherSets : transitives){
            if(sets.getRight().containsAll(otherSets.getLeft())){
              FD temp = new FD(sets.getLeft(), otherSets.getRight());
              if(!mySets.contains(temp)){
                transitives.add(temp);
              }
            }
        }
      }
      if(transitives.equals(check)){
        break;
      }
    }

    return transitives;
  }

  /**
   * Generates the closure of the input FD set, omitting FDs with empty
   * right-hand sides. The relation's attributes are assumed to be exactly
   * those appearing on either side of the input FDs.
   *
   * @param fdset input FD set; must not be modified
   * @return a new FD set containing the closure
   */
  public static FDSet fdSetClosure(final FDSet fdset) {
    FDSet current = new FDSet(fdset); // make a copy of F
    for(FD sets : current){
      current.addAll(trivial(sets.getLeft()));
      current.addAll(trivial(sets.getRight()));
    }
    
    while(true){
      FDSet check = new FDSet(current);
      for(FD sets : current){
        current.addAll(augment(current, sets.getLeft()));
        current.addAll(augment(current, sets.getRight()));
      }
      current.addAll(transitive(current));
      if(check.equals(current)){
        break;
      }
    }
    return current;
  }

  /**
   * Generates all subsets of the input set, including the empty subset.
   *
   * @param set input set; must not be modified
   * @return the power set of the input set
   */
  public static <E> Set<Set<E>> powerSet(final Set<E> set) {
    if(set.isEmpty()){
      HashSet<Set<E>> emptySetOfSets = new HashSet<>();
      emptySetOfSets.add(new HashSet<>());
      return emptySetOfSets;
    }

    Object[] currSet = set.toArray();
    E currFirst = (E) currSet[0];
    
    HashSet<Set<E>> currPowerSet = new HashSet<>();
    HashSet<E> recurseSet = new HashSet<>(set);
    recurseSet.remove(currSet[0]);

    for(Set<E> recurse : powerSet(recurseSet)){
        HashSet<E> mySet = new HashSet<>();
        mySet.add(currFirst);
        mySet.addAll(recurse);
        currPowerSet.add(mySet);
        currPowerSet.add(recurse);
    }

    return currPowerSet;
  }
}