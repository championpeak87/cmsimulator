var express = require('express');
var router = express.Router();

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
  password: '3656a5ec8d234e9cbab536bdd251b8cb',
  port: 5432
})

// REGISTRACIA POUZIVATELA
router.get('/signup', (req, res) => {
  const username = req.query.username;
  const first_name = req.query.first_name;
  const last_name = req.query.last_name;
  const auth_key = req.query.auth_key;
  const user_type = req.query.user_type;
  const salt = req.query.salt;

  // KONTROLA CI POUZIVATEL UZ NEEXISTUJE
  pool.query('SELECT * FROM users WHERE username = $1', [username], (error, results) => {


    // POUZIVATEL EXISTUJE
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send("USER EXISTS!");
      console.log(Date() + ' ', [username], 'already exists!');
    }
    else {
      var query = 'INSERT INTO users(username, user_type, password_hash, first_name, last_name, salt) VALUES (\'' + [username] + '\', \'' + [user_type] + '\', E\'\\\\x' + auth_key + '\', \'' + [first_name] + '\', \'' + [last_name] + '\', E\'\\\\x' + salt + '\');';
      console.log(query);
      pool.query(query, (err, result) => {

        res.status(HTTP_OK).send('USER WAS ADDED!');
        console.log(Date() + ' ', [username], 'was successfully added to database');
      });
    }
  })
})

// ZMENA HESLA
router.get('/changePassword', (req, res) => {
  const user_id = req.query.user_id;
  const auth_key = req.query.auth_key;
  const new_auth_key = req.query.new_auth_key;

  pool.query('SELECT * FROM users WHERE user_id = $1;', [user_id], (error, results) => {


    if (results.rowCount > 0) {
      console.log(Date() + ' ', [results.rows[0].username], 'has changed password.');

      var query = 'UPDATE public.users SET password_hash=E\'\\\\x' + new_auth_key + '\' WHERE user_id=' + user_id + ';';
      pool.query(query, (err, result) => {
        res.status(HTTP_OK).send('Password changed');
      })
    }
    else {
      console.log(Date() + 'Unable to perform requested operation!');
      res.status(HTTP_NOT_FOUND).send('User was not found!');
    }
  })
})

router.get('/getCount', (req, res) => {
  pool.query('SELECT count(*) FROM users;', (err, results) => {
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send(
        {
          count: results.rows[0].count
        }
      );
    }
  });
});

// PRIHLASENIE
router.get('/login', (req, res) => {
  const username = req.query.username;
  const password = req.query.auth_key;


  pool.query('select user_id, user_type, username, first_name, last_name, encode(password_hash::bytea, \'hex\') as password_hash, encode(salt::bytea, \'hex\') as salt from users where username = $1;', [username], (error, results) => {
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
router.get('/login_salt', (req, res) => {
  const username = req.query.username;

  pool.query('SELECT encode(salt::bytea, \'hex\') as salt FROM users WHERE username = $1;', [username], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0) {
      res.status(HTTP_OK).send({
        username: username,
        salt: results.rows[0].salt
      })
    }
    else {
      res.status(HTTP_NOT_FOUND).send("Selected user was not found!");
    }
  })
})

router.get('/update', (req, res) => {
  const logged_user_id = req.query.logged_user_id;
  const auth_key = '\\x' + req.query.auth_key;
  const user_id = req.query.user_id;
  const first_name = req.query.first_name;
  const last_name = req.query.last_name;
  const password_hash = req.query.password_hash;
  const type = req.query.type;
  const username = req.query.username;

  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_id = $2 AND user_type = \'admin\';', [auth_key, logged_user_id], (err, results) => {
    if (results.rowCount > 0) {
      pool.query('UPDATE users SET username=$1, user_type=$2, first_name=$3, last_name=$4 WHERE user_id=$5;', [username, type, first_name, last_name, user_id], (error, result) => {
        if (result.rowCount > 0) {
          res.status(HTTP_OK).send({
            admin_id: logged_user_id,
            user_id: user_id,
            first_name: first_name,
            last_name: last_name,
            user_type: type,
            username: username,
            updated: true
          });
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            updated: false
          });
        }
      })
    }
  });
})

router.get('/delete', (req, res) => {
  const logged_user_id = req.query.logged_user_id;
  const auth_key = '\\x' + req.query.auth_key;
  const user_id = req.query.user_id;
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_id = $2 AND user_type = \'admin\';', [auth_key, logged_user_id], (err, results) => {
    if (results.rowCount > 0) {
      pool.query('DELETE FROM users WHERE user_id = $1;', [user_id], (error, result) => {
        if (result.rowCount > 0) {
          res.status(HTTP_OK).send(
            {
              user_id: user_id,
              deleted: true
            }
          );
        }
        else {
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

router.get('/getUsers', (req, res) => {
  const auth_key = '\\x' + req.query.auth_key;
  const offset = req.query.offset;
  console.log([auth_key]);
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_type = \'admin\';', [auth_key], (err, results) => {
    if (results.rowCount > 0) {
      pool.query('SELECT * FROM users LIMIT 20 OFFSET $1;', [offset], (error, foundUsers) => {
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

router.get('/filterUsers', (req, res) => {
  const order_by = req.query.order_by;
  const ascending = req.query.ascending;

  if (order_by == 'username') {
    if (ascending == 'true') {
      pool.query('SELECT * FROM users ORDER BY username ASC;', (err, results) => {
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    } else {
      pool.query('SELECT * FROM users ORDER BY username DESC;', (err, results) => {
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    }
  } else if (order_by == 'last_name') {
    if (ascending == 'true') {
      pool.query('SELECT * FROM users ORDER BY last_name ASC;', (err, results) => {

        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    } else {
      pool.query('SELECT * FROM users ORDER BY last_name DESC;', (err, results) => {

        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    }
  } else if (order_by == 'first_name') {
    if (ascending == 'true') {
      pool.query('SELECT * FROM users ORDER BY first_name ASC;', (err, results) => {

        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    } else {
      pool.query('SELECT * FROM users ORDER BY first_name DESC;', (err, results) => {

        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    }
  }
})

router.get('/findUsers', (req, res) => {
  const string = req.query.string;
  const find_by = req.query.find_by;

  var search_string = '%' + string + '%';
  if (find_by == 'first_name') {
    pool.query('SELECT * FROM users WHERE first_name LIKE $1;', [search_string], (err, results) => {

      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
      }
      else res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    });
  } else if (find_by == 'last_name') {
    pool.query('SELECT * FROM users WHERE last_name LIKE $1;', [search_string], (err, results) => {

      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
      }
      else res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    });
  } else if (find_by == 'username') {
    pool.query('SELECT * FROM users WHERE username LIKE $1;', [search_string], (err, results) => {

      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
      }
      else res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    });
  }

});

router.get('/getUsersFiltered', (req, res) => {
  const auth_key = '\\x' + req.query.auth_key;
  const last_name = req.query.last_name;
  console.log([auth_key]);
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_type = \'admin\';', [auth_key], (err, results) => {

    if (results.rowCount > 0) {
      pool.query('SELECT * FROM users WHERE last_name LIKE \'%' + last_name + '%\';', (error, foundUsers) => {

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


module.exports = router;
