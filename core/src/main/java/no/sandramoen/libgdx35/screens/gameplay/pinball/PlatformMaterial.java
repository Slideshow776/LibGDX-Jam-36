package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.math.Vector2;

public enum PlatformMaterial {

    WOOD("images/platform_wood.png", 0.75f, 0.35f) {
        @Override
        public void onBeginContact(BallActor ball, PlatformActor platform) {
        }

        @Override
        public boolean shouldDisableContact(BallActor ball, PlatformActor platform, float ballRadius) {
            return shouldDisableOneWayContact(ball, platform, ballRadius);
        }
    },

    GLASS("images/platform_glass.png", 0.02f, 0.05f) {
        @Override
        public void onBeginContact(BallActor ball, PlatformActor platform) {
            platform.breakPlatform();
        }

        @Override
        public boolean shouldDisableContact(BallActor ball, PlatformActor platform, float ballRadius) {
            return true;
        }
    },

    METAL("images/platform_metal.png", 1.05f, 0.1f) {
        @Override
        public void onBeginContact(BallActor ball, PlatformActor platform) {
            Vector2 velocity = ball.getBody().getLinearVelocity();
            ball.getBody().setLinearVelocity(velocity.x, Math.max(velocity.y, 0f) + 1.25f);
        }

        @Override
        public boolean shouldDisableContact(BallActor ball, PlatformActor platform, float ballRadius) {
            return shouldDisableOneWayContact(ball, platform, ballRadius);
        }
    },

    GUM("images/platform_gum.png", 0.18f, 1.1f) {
        @Override
        public void onBeginContact(BallActor ball, PlatformActor platform) {
            Vector2 velocity = ball.getBody().getLinearVelocity();
            ball.getBody().setLinearVelocity(velocity.x * 0.85f, velocity.y * 0.85f);
        }

        @Override
        public boolean shouldDisableContact(BallActor ball, PlatformActor platform, float ballRadius) {
            return shouldDisableOneWayContact(ball, platform, ballRadius);
        }
    };

    private final String texturePath;
    private final float restitution;
    private final float friction;

    PlatformMaterial(String texturePath, float restitution, float friction) {
        this.texturePath = texturePath;
        this.restitution = restitution;
        this.friction = friction;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public float getRestitution() {
        return restitution;
    }

    public float getFriction() {
        return friction;
    }

    public abstract void onBeginContact(BallActor ball, PlatformActor platform);

    public abstract boolean shouldDisableContact(BallActor ball, PlatformActor platform, float ballRadius);

    protected static boolean shouldDisableOneWayContact(BallActor ball, PlatformActor platform, float ballRadius) {
        float ballBottom = ball.getBody().getPosition().y - ballRadius;
        float platformTop = platform.getBody().getPosition().y + platform.getPlatformHeight() * 0.5f;
        float velocityY = ball.getBody().getLinearVelocity().y;
        return velocityY > 0f && ballBottom < platformTop - 0.02f;
    }
}
