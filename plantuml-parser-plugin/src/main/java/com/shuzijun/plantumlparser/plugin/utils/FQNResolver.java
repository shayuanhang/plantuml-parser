package com.shuzijun.plantumlparser.plugin.utils;

import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.plugin.utils.impl.CacheFQNResolver;

public interface FQNResolver {
    static FQNResolver getInstance(){
        return CacheFQNResolver.getInstance();
    }

    /**
     * 如果返回null，代表非class,可能是package-info
     * @param code
     * @return
     */
    String getFQN(Code code);
}
