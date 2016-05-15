package fi.babywellness.babyhale;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public interface UpdatableTTS {
    void updateTTS(Fragment calledFrom, Bundle arguments);
}
