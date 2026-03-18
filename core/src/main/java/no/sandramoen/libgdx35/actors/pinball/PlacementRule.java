package no.sandramoen.libgdx35.actors.pinball;

public enum PlacementRule {
    LEFT_BUMPER_RULE(PlacementState.LEFT_BUMPER, new PlacementState[]{PlacementState.RIGHT_CLIFF}), RIGHT_BUMPER_RULE(PlacementState.RIGHT_BUMPER, new PlacementState[]{PlacementState.LEFT_CLIFF}), DOUBLE_BUMPER_RULE(PlacementState.DOUBLE_BUMPER, new PlacementState[]{PlacementState.LEFT_CLIFF, PlacementState.RIGHT_CLIFF}), LEFT_CLIFF_RULE(PlacementState.LEFT_CLIFF, new PlacementState[]{PlacementState.RIGHT_BUMPER, PlacementState.DOUBLE_BUMPER}), RIGHT_CLIFF_RULE(PlacementState.RIGHT_CLIFF, new PlacementState[]{PlacementState.LEFT_BUMPER, PlacementState.DOUBLE_BUMPER});


    private final PlacementState state;
    private final PlacementState[] allowed;

    PlacementRule(PlacementState state, PlacementState[] allowed) {
        this.state = state;
        this.allowed = allowed;
    }

    public PlacementState getState() {return state;}

    public PlacementState[] getAllowed() {return allowed;}

    public static PlacementRule getRule(PlacementState state) {
        for (PlacementRule rule : PlacementRule.values()) {
            if (rule.state == state) return rule;
        }
        return null;
    }
}
