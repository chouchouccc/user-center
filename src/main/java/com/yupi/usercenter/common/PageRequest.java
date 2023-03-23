package com.yupi.usercenter.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 7132556857756071421L;
    /**
     * 一页多少
     */
    protected int pageSize;
    /**
     * 一页多少个
     */
    protected int pageNum;

}
