import { Injectable } from "@angular/core";

@Injectable({
    providedIn: 'root' // ensures it's a singleton across the app
})

export class Globals {
   //currentUser: any = undefined;

    getUser() {
        const userId = localStorage.getItem('userId');
        if(userId) {
            return JSON.parse(userId)
        }
        
        return null;
    }

    setUser(newObj: any) {
        localStorage.setItem('userId', newObj);
        //this.currentUser = newObj;
    }

}