package cn.linshio.community.entity;

import lombok.Getter;

//数据分页实体类
@Getter
public class Page {
    //当前的页码
    private int current = 1;
    //显示当前页的上限
    private int limit = 10;
    //数据的总数 - 计算总页数
    private int rows;
    //查询路径
    private String path;

    //获取当前页的起始行
    public int getCurrentOffset(){
        //current*limit - limit
        return (current-1)*limit;
    }
    //获取当前的总页数
    public int getCurrentTotal(){
        //如果总条数除以每页条数不能整除那就多一页
        if (rows%limit==0){
            return rows/limit;
        }else {
            return rows/limit+1;
        }
    }

    // 获取起始页码
    public int getBeginPage(){
        int beginPage = current -2;
        return Math.max(beginPage, 1);
    }
    //获取终止页码
    public int getEndPage(){
        int endPage = current +2;
        int totalPage = getCurrentTotal();
        return Math.min(endPage,totalPage);
    }


    //检测当前页是否合法
    public void setCurrent(int current) {
        if (current>1){
            this.current = current;
        }
    }

    //检测当前显示当页总数是否合法
    public void setLimit(int limit) {
        if (limit>=1 && limit<=100){
            this.limit = limit;
        }
    }

    //检测当前页的总条数是否合法
    public void setRows(int rows) {
        if (rows>=0){
            this.rows = rows;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }
}
