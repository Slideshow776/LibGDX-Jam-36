package no.sandramoen.libgdx35.utils;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/*
 * "Load a simple text file through asset manager in libgdx"
 *
 * Copied from @author: RegisteredUser
 * https://gamedev.stackexchange.com/questions/101326/load-a-simple-text-file-through-asset-manager-in-libgdx
 */
public class TextLoader extends AsynchronousAssetLoader<no.sandramoen.libgdx35.utils.Text, TextLoader.TextParameter> {

    public TextLoader(FileHandleResolver resolver) {

        super(resolver);

    }

    no.sandramoen.libgdx35.utils.Text text;

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, TextParameter parameter) {

        this.text = null;
        this.text = new no.sandramoen.libgdx35.utils.Text(file);

    }

    @Override
    public no.sandramoen.libgdx35.utils.Text loadSync(AssetManager manager, String fileName, FileHandle file, TextParameter parameter) {

        no.sandramoen.libgdx35.utils.Text text = this.text;
        this.text = null;

        return text;

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TextParameter parameter) {

        return null;

    }

    public static class TextParameter extends AssetLoaderParameters<no.sandramoen.libgdx35.utils.Text> {

    }

}
