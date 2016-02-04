# AutoJMH

**AutoJMH** is a tool that takes as input one segment of code (belonging to an existing program) and one training execution (could be a unit test run for example) and builds full JMH microbenchmark for the segment. The tool generates the JMH class and then places the segment code inside the wrapper method, fully generating the microbenchmark. **AutoJMH** also initializes the variables of the microbenchmark and perform a series of analysis to automatically avoid common microbenchmarking pitfalls.

For example, suppose that given the following program we want to extract the `int a = (int)Math.log(n * n / 2) * myBubbleSort(list)` into a microbenchmark:

    private static int gnomesort(List<Integer> ar) {
        int i = 0; int k = 0;
        while (i < ar.size()) {
            if (i == 0 || ar.get(i-1) <= ar.get(i)) i++;
            else { int tmp = ar.get(i); ar.set(i, ar.get(i-1)); ar.set(--i, tmp); }
            k++;
        }
        return k;
    }

    //Sorts the list, add 'n' and returns the number 
    //of steps needed to sort the list and do some math with it
    public int addAndSorted(List<Integer> list, int n) {
		n = n < 1 ? 1 : n;
        list.add(n);

        /** @bench-this */
        int a = (int)Math.log(n * n / 2) * myBubbleSort(list);
        
        return a;  
    }

**AutoJMH** will generate the following microbenchmark for it:


    @State(Scope.Thread)
    public class org_packagename_YourClassName_348 {
        int n;
        List<Integer> list;
        List<Integer> list___reset;

        private static int gnomesort(List<Integer> ar) {
            int i = 0; int k = 0;
            while (i < ar.size()) {
                if (i == 0 || ar.get(i-1) <= ar.get(i)) i++;
                else { int tmp = ar.get(i); ar.set(i, ar.get(i-1)); ar.set(--i, tmp); }
                k++;
            }
            return k;
        }

        @Setup
        public void setup() {
            Loader loader_348 = new Loader("usr/data/YourClassName_348");
			      list___reset = loader_348.readIntegerList();
            list = loader_348.readEmptyList(); 
			      n = loader_348.readInt();
        }

        @Setup(Level.Invocation)
        public void resetCode() {
            list.clear(); list.addAll(list___reset);
        }

        @Benchmark
        public int doBenchmark() {
            int a = (int)Math.log(n * n / 2) * gnomesort(list);
            return a;
        }
    }
    
The tool is able to extract the code and all its dependencies (method, variables), provide initialization values, avoid DCE and keep the microbenchmark in its one stable state.
