package com.gdiama.client.enhancer;

import com.gdiama.client.Config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelRequestor implements InvocationHandler { //,Lifcycle.Shutdownable {
    private  List<String> allowedMethods = new ArrayList<>();
    private  ExecutorCompletionService<Object> completionService;
//    private final ExecutorShutdown executorShutdown;

//    public ParallelRequestor(Client client, Config config, ShutdownService shutdownService) {
//        this.allowedMethods.addAll(collectMethodNames(indexHttpClient));
//        ExecutorService executorService = Executors.newFixedThreadPool(20);
////        executorShutdown = new ExecutorShutdown(executorService, config);
//        completionService = new ExecutorCompletionService<>(executorService);
//        shutdownService.register(this);
//    }
//
//    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
//        if (!allowedMethods.contains(method.getName())) {
//            throw new IllegalArgumentException("No 'event' or 'query' method");
//        }
//
//        List<Future> futures = new ArrayList<>();
//
//        for (final Object arg : args) {
//            futures.add(completionService.submit(new Callable<Object>() {
//                @Override
//                public Object call() throws Exception {
//                    return method.invoke(indexHttpClient, arg);
//                }
//            }));
//        }
//
//        Object result = null;
//        if (method.getName().equals("query")) {
//            QueryResponse queryResponse = new QueryResponse();
//            queryResponse.indexes = new ArrayList<>();
//            queryResponse.items = new ArrayList<>();
//
//            Future<Object> future = completionService.poll();
//            if (future != null) {
//                QueryResponse singleQueryResponse;
//                try {
//                    singleQueryResponse = (QueryResponse) future.get(200, TimeUnit.MILLISECONDS);
//                    queryResponse.indexes.addAll(singleQueryResponse.indexes);
//                    queryResponse.items.addAll(singleQueryResponse.items);
//                    result = queryResponse;
//                } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    result = queryResponse;
//                }
//            }
//        } else if (method.getName().equals("indexItem")) {
//            IndexItemResponse response = new IndexItemResponse();
//            response.indexes = new ArrayList<>();
//
//            Future<Object> future = completionService.poll();
//            if (future != null) {
//                IndexItemResponse indexItemResponse;
//                try {
//                    indexItemResponse = (IndexItemResponse) future.get(20, TimeUnit.MILLISECONDS);
//                    response.indexes.addAll(indexItemResponse.indexes);
//                    result = response;
//                } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    result = response;
//                }
//            }
//        } else {
//            throw new IllegalArgumentException("Not handling other methods yet");
//        }
//
//        return result;
        return null;
    }
//
//    private List<String> collectMethodNames(IndexServiceManagerHttpClient indexHttpClient) {
//        List<String> allowedMethods = new ArrayList<>();
//        Method[] declaredMethods = indexHttpClient.getClass().getDeclaredMethods();
//        for (Method declaredMethod : declaredMethods) {
//            allowedMethods.add(declaredMethod.getName());
//        }
//        return allowedMethods;
//    }
//
//    @Override
//    public void shutdown() throws Exception {
//        executorShutdown.shutdown();
//    }
}
