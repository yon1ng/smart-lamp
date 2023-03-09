package cn.liuyong.smartlamp.vo;

import cn.liuyong.smartlamp.constant.ResultCode;

import java.io.Serializable;

public class ResultVO implements Serializable {

    private static final long serialVersionUID = 1545984421992L;

    @Override
    public String toString() {
        return "ResultVO{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    private String code;

    private String msg;

    private Object data;

    public ResultVO(Object data) {
        this.code = ResultCode.SUCCESS.getCode();
        this.data = data;
    }

    public ResultVO(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultVO(String code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public ResultVO() {

    }

    public ResultVO(Object data, String msg) {
        this.code = ResultCode.SUCCESS.getCode();
        this.data = data;
        this.msg = msg;
    }

    public static ResultVO success(Object data, String msg) {
        return new ResultVO(data, msg);
    }

    public static ResultVO error(Object data, String msg) {
        ResultVO result = new ResultVO(data, msg);
        result.setCode(ResultCode.ERROR.getCode());

        return result;
    }

    public static ResultVO error(String msg) {
        ResultVO result = new ResultVO(null, msg);
        result.setCode(ResultCode.ERROR.getCode());

        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg != null ? msg : "";
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }


    public void setData(Object data) {
        this.data = data;
    }
}
