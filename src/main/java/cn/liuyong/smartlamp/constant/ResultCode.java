package cn.liuyong.smartlamp.constant;


public enum ResultCode {

    SUCCESS("处理成功", "0000"),


    ERROR("处理失败", "9999")
    ;


    private String name;

    private String code;

    private ResultCode(String name, String code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code + "_" + this.name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

}
