const express = require('express');
const bodyParser = require('body-parser');
const filesystem = require('fs');
const app = express();
var fileuploader = require('express-fileupload');
app.use(fileuploader());
const port = 3000

// HTTP STAVOVE KODY
/// USPESNE
const HTTP_OK = 200
const HTTP_CREATED = 201
const HTTP_ACCEPTED = 202
const HTTP_NO_CONTENT = 204

/// CHYBA KLIENTA
const HTTP_BAD_REQUEST = 400
const HTTP_UNAUTHORIZED = 401
const HTTP_FORBIDDEN = 403
const HTTP_NOT_FOUND = 404

const Pool = require('pg').Pool
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'cmsimulator',
  password: '123456',
  port: 5432,
})

app.use(express.json());
app.use(bodyParser.json())
app.use(
  bodyParser.urlencoded({
    extended: true,
  })
)


// REGISTRACIA POUZIVATELA
app.get('/api/user/signup', (req, res) => {
  const username = req.query.username;
  const first_name = req.query.first_name;
  const last_name = req.query.last_name;
  const auth_key = req.query.auth_key;
  const user_type = req.query.user_type;
  const salt = req.query.salt;

  // KONTROLA CI POUZIVATEL UZ NEEXISTUJE
  pool.query('SELECT * FROM users WHERE username = $1', [username], (error, results) => {
    if (error) {
      throw error;
    }

    // POUZIVATEL EXISTUJE
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send("USER EXISTS!");
      console.log(Date() + ' ', [username], 'already exists!');
    }
    else {
      pool.query('INSERT INTO users(username, type, password_hash, first_name, last_name, salt) VALUES ($1,$2,$3,$4,$5,$6);',
        [username, user_type, auth_key, first_name, last_name, salt], (err, result) => {
          res.status(HTTP_OK).send('USER WAS ADDED!');
          console.log(Date() + ' ', [username], 'was successfully added to database');
        });
    }
  })
})



// ZMENA HESLA
app.get('/api/user/changePassword', (req, res) => {
  const user_id = req.query.user_id;
  const auth_key = req.query.auth_key;
  const new_auth_key = req.query.new_auth_key;

  pool.query('SELECT * FROM users WHERE user_id = $1;', [user_id], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0) {
      if (results.rows[0].password_hash == auth_key) {
        console.log(Date() + ' ', [results.rows[0].username], 'has changed password.');

        pool.query('UPDATE public.users SET password_hash=$1 WHERE user_id=$2;', [new_auth_key, user_id], (err, result) => {
          if (error) {
            throw error;
          }
          res.status(HTTP_OK).send('Password changed');
        })

      }
      else {
        console.log(Date() + ' ', [results.rows[0].username], 'Unable to authentificate user!');
        res.status(HTTP_FORBIDDEN).send('Unable to authentificate user!');
      }
    }
    else {
      console.log(Date() + 'Unable to perform requested operation!');
      res.status(HTTP_NOT_FOUND).send('User was not found!');
    }
  })
})


// PRIHLASENIE
app.get('/api/login', (req, res) => {
  const username = req.query.username;
  const password = req.query.auth_key;

  pool.query('SELECT * FROM users WHERE username = $1;', [username], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0) {
      if (results.rows[0].password_hash == password) {
        console.log(Date() + ' ', [username], 'has successfully logged in.');
        res.status(HTTP_OK).send(results.rows[0]);
      }
      else {
        console.log(Date() + ' ', [username], 'was unable to log in.');
        res.status(HTTP_FORBIDDEN).send('Unable to log in!');
      }
    }
    else {
      console.log(Date() + 'Unable to perform requested operation!');
      res.status(HTTP_NOT_FOUND).send('User was not found!');
    }
  })
})

// ZISKANIE SALTU
app.get('/api/login_salt', (req, res) => {
  const username = req.query.username;

  pool.query('SELECT salt FROM users WHERE username = $1;', [username], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0)
    {
      res.status(HTTP_OK).send({
        username: username,
        salt: results.rows[0].salt
      })
    }
    else
    {
      res.status(HTTP_NOT_FOUND).send("Selected user was not found!");
    }
  })
})

app.get('/api/tasks/automata', (req, res) => {
  pool.query('SELECT * FROM automata_tasks;', (error, results) => {
    if (error) { throw error }
    if (results.rowCount > 0) {
      res.send(results.rows);
    }
  })
})

app.get('/api/user/update', (req, res) =>
{
  const logged_user_id = req.query.logged_user_id;
  const auth_key = req.query.auth_key;
  const user_id = req.query.user_id;
  const first_name = req.query.first_name;
  const last_name = req.query.last_name;
  const password_hash = req.query.password_hash;
  const type = req.query.type;
  const username = req.query.username;

  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_id = $2 AND type = \'admin\';', [auth_key, logged_user_id], (err, results) => {
    if ( err ) { throw err }
    if (results.rowCount > 0)
    {
      pool.query('UPDATE users SET username=$1, type=$2, password_hash=$3, first_name=$4, last_name=$5 WHERE user_id=$6;', [username, type, password_hash, first_name, last_name, user_id], (error, result) =>
      {
        if ( error ) { throw error }
        if (result.rowCount > 0)
        {
          res.status(HTTP_OK).send({
            admin_id: logged_user_id,
            user_id: user_id,
            first_name: first_name,
            last_name: last_name,
            type: type,
            username: username,
            updated: true
          });
        }
        else
        {
          res.status(HTTP_NOT_FOUND).send({
            updated: false
          });
        }
      })
    }
  });
})

app.get('/api/user/delete', (req, res) =>
{
  const logged_user_id = req.query.logged_user_id;
  const auth_key = req.query.auth_key;
  const user_id = req.query.user_id;
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_id = $2 AND type = \'admin\';', [auth_key, logged_user_id], (err, results) =>
  {
    if ( err ) { throw err }
    if (results.rowCount > 0)
    {
      pool.query('DELETE FROM users WHERE user_id = $1;', [user_id], (error, result) =>
      {
        if ( error ) { throw error }
        if (result.rowCount > 0)
        {
          res.status(HTTP_OK).send(
            {
              user_id: user_id,
              deleted: true
            }
          );
        }
        else
        {
          res.status(HTTP_NOT_FOUND).send(
            {
              user_id: user_id,
              deleted: false
            }
          );
        }
      })
    }
  })
})

app.get('/api/user/getUsers', (req, res) => {
  const auth_key = req.query.auth_key;
  const offset = req.query.offset;
  console.log([auth_key]);
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND type = \'admin\';', [auth_key], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('SELECT * FROM users LIMIT 20 OFFSET $1;', [offset], (error, foundUsers) => {
        if (error) { throw error }
        if (foundUsers.rowCount > 0) {
          res.status(HTTP_OK).send(foundUsers.rows);
          console.log("All users have been requested!");
        }
      })
    }
    else {
      res.status(HTTP_FORBIDDEN).send("You are not authorised to perform this operation!");
      console.log("Unauthorised request to download all users!");
    }
  })
})

app.get('/api/user/getUsersFiltered', (req, res) => {
  const auth_key = req.query.auth_key;
  const last_name = req.query.last_name;
  console.log([auth_key]);
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND type = \'admin\';', [auth_key], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('SELECT * FROM users WHERE last_name LIKE \'%' + last_name + '%\';', (error, foundUsers) => {
        if (error) { throw error }
        if (foundUsers.rowCount > 0) {
          res.status(HTTP_OK).send(foundUsers.rows);
          console.log("User search performed!");
        }
      })
    }
    else {
      res.status(HTTP_FORBIDDEN).send("You are not authorised to perform this operation!");
      console.log("Unauthorised request to download all users!");
    }
  })
})

app.get('/api/tasks/delete', (req, res, next) => {
  const task_id = req.query.task_id;

  pool.query('SELECT * FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) =>
  {
    if (err) {throw err}
    if (result.rowCount > 0)
    {
      const filename = "./uploads/automataTasks/" + result.rows[0].file_name;
      filesystem.unlink(filename, (error) =>
      {
        if (error) {throw error}
      });
    }
  })
  pool.query('DELETE FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) =>
  {
    if (err) {throw err}
    if (result.rowCount > 0)
    {
      console.log([task_id], "Task has been deleted!");
      res.status(HTTP_OK).send({
        task_id: task_id,
        deleted: true
      })
    }
    else
    {
      res.status(HTTP_OK).send({
        task_id: task_id,
        deleted: false
      })
    }
  })
})

app.post('/api/tasks/upload', (req, res, next) => {
  const file = req.files.task;
  const file_name = req.query.file_name;

  var mkdirp = require('mkdirp');
  mkdirp("./uploads/", function (err) { });
  mkdirp("./uploads/automataTasks/", function (err) { });
  if (!file) {
    console.log(req);
    console.log("FILE NOT SAVED!");
  }
  else {
    file.mv("./uploads/automataTasks/" + file.name, function (err, results) {
      if (err) throw err;
      res.send({
        success: true,
        message: "File uploaded!"
      });
      console.log("NEW TASK UPLOADED!");
    });
  }

});

app.get('/api/tasks/add', (req, res) => {
  const task_name = req.query.task_name;
  const task_description = req.query.task_description;
  const time = req.query.time;
  const assigner = req.query.assigner;
  const public_input = req.query.public_input;
  const file_name = req.query.file_name;
  const automata_type = req.query.automata_type;


  pool.query('INSERT INTO automata_tasks(assigner_id, file_name, public_input, automata_type, task_description, task_name, time) VALUES ($1, $2, $3, $4, $5, $6, $7);',
    [assigner, file_name, public_input, automata_type, task_description, task_name, time], (err, results) => {
      if (err) { throw err };

      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(
          {
            task_name: task_name,
            task_description: task_description,
            time: time,
            public_input: public_input,
            file_name: file_name,
            automata_type: automata_type
          }
        )
      }
    });

})

app.get('/api/tasks/getTasks', (req, res) => {
  const user_id = req.query.user_id;
  const auth_key = req.query.auth_key;

  pool.query('SELECT * FROM automata_tasks;', (err, results) => {

    if (err) { throw err }
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send(results.rows);
      console.log(Date(), [user_id], 'has fetched tasks!');
    }
    else{
      res.status(HTTP_FORBIDDEN).send("FORBIDDEN!");
    }
  })
})

app.get('/api/tasks/download', (req, res) =>
{
  const task_id = req.query.task_id;

  pool.query('SELECT file_name FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) =>
  {
    if (err) {throw err}
    {
      if (result.rowCount > 0)
      {
        const filePath = "./uploads/automataTasks/" + result.rows[0].file_name;
        res.status(HTTP_OK).download(filePath);
      }
      else
      {
        res.status(HTTP_NOT_FOUND).send(
          {
            task_id: task_id,
            found: false
          }
        );
      }
    }
  })
})

app.listen(port, () => console.log(`CMServer server listening on port ${port}!`))