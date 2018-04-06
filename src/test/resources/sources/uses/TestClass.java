public class TestClass {

    public static class UsedInDistance2 {

    }

    public static class Used {
        public UsedInDistance2 method() {
            return null;
        }
    }

    public Used userMethod() {
        return null;
    }

    public static class UseCycleStart {
        public UseCycleEnd method() {
            return null;
        }
    }

    public static class UseCycleEnd {
        public UseCycleStart method() {
            return null;
        }
    }

    public void cycleMethod(UseCycleStart useCycle) {
    }
}
