# Backend Coding Guidelines

- Return **boolean**'s, **Optional**'s or **Error Types** in favour of ***throwing Exceptions***.  
  This makes for easier to handle and **propogate Errors**, instead of just ***ignoring them***.  
- Adding Dependencies may break code in **weird and unexpected manners**, so please **consult the maintainer** before adding dependencies. 
- Try to follow the **[Oracle Java Coding Guidelines](https://www.oracle.com/java/technologies/javase/seccodeguide.html)** as best as possible.
- *Extract functionality* that may be **interesting for multiple pieces of code** into a ***seperate Utility Class***.

## Model Coding Guidelines
- Database Column Names should be in ***Snake Case***.
- Database Table Names should be in ***Snake Case***.
- Use sensible values for the **nullable** and **optional** Arguments.
- **All** Model-**Fields** should have ***Getters***.  
  **Setters** should be created in ***sensible situations*** (but not for *Collections, Lists, Sets, Maps, etc.* because they should be **modified using** ***add*** and ***remove***)
- toString **should not contain** any ***implementation specific*** or ***database*** information.  
  e.g. The ID of the Model in the Database or an internal Flag.
- equals and hashCode should use the ***simplest and smallest amount of fields*** of the model that **ensure uniqueness**.  
  e.g. The Database ID or (a unique) username.
- **ID Fields** should be of ***Type UUID*** and ***auto-generated*** by the Database using the **@GeneratedValue**-Annotation.
- **Never implicitly** send *Collections, Lists, Sets, Maps, etc.* with a Request, the Front-End should ***request them seperately***.  
  Sometimes implicitly sending them makes sense, but it is **discouraged in most cases**.

## Service Coding Guidelines
- Create private versions of methods that **do not check permissions** and then create ***Wrapper-Methods that ensure the correct Permissions*** e.g.
```java
public static class Example {
  private static T doStuff(SomeModel m, AnotherModel a) {
    // Modifying some variables
    // Changing stuff around
    return t;
  }

  public static boolean doStuffSafely(SomeModel m, AnotherModel a) {
    if (/* Check Permissions are met */ 1) {
      var result = doStuff(m, a);
      return (t == valid);
    }
    return false;
  }
}
```
- The **private Methods** should take the ***Models*** that are going to change **directly as its Arguments**.
- Create sensible public Wrappers and associated Overloads for the private Methods.
```java
public static class Example {
  public static boolean doStuffSafely(SomeModelId mId, AnotherModelId aId) {
    if (/* Check Permissions are met */ 1) {
      SomeModel m = getSomeModelById(mId);
      AnotherModel a = getAnotherModelById(aId);
      var result = doStuff(m, a);
      return (t == valid);
    }
    return false;
  }

  public static boolean doStuffSafely(SomeModelId mId) {
    if (/* Check Permissions are met */ 1) {
      SomeModel m = getSomeModelById(mId);
      AnotherModel a = getCurrentAnotherModel();
      var result = doStuff(m, a);
      return (t == valid);
    }
    return false;
  }
}
```

## Controller Coding Guidelines
- **Avoid long methods**, instead try to call the ***appropriate Service-Methods***.
- Always return JSON from Endpoints using the **RestResponse** as a Base Class.  
  This ensures that the Front-End can know the Response-Type and whether the Operation failed or sucedded on the Backend.  
- The **Argument List of an Endpoint** should contain the ***minimal/simplest set of Arguments needed*** for the Operation.
- Always check which ***Permissions*** are needed for a User to access the Endpoint.  
  Preferably use our **custom Annotations** or [PreAuthorize](https://www.appsdeveloperblog.com/spring-security-preauthorize-annotation-example/).
- Choose the **most appropiate HTTP-Method** for ***each Endpoints***.  
  See [MSDN Docs](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods):
  - ***GET***  
    Used when accessing Resources or ***fetching*** Information.
  - ***PUT***  
    Used when ***creating*** a resource.
  - ***POST***  
    Used when submitting a resource to the server.  
    This will often trigger a ***side effect*** on the ***server-side***.
  - ***PATCH***  
    Used when ***partially changing*** to a resource.
  - ***DELETE***  
    Used when ***deleting*** a resource.  

## Config Coding Guidelines
- **Just try not to break it (^_^)**
- Always ensure that any changes in the Config ***do not break the AuthContext***.  
  Breaking the AuthContext means **breaking pretty much every Service-Method**.
- Be ***as strict as possible***.  
  If there is ambuigity in how something can be implemented use the way that will **produce false-negatives** instead of false-positives.  
This ensures that user's cannot accidentaly call code they should not be able to call.
- The Config is by far the most complicated and obscure part of the code, so comment anything that is ***not implicitly obvious***.
